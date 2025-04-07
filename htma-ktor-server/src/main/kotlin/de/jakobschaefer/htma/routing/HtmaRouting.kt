package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.htmaConfiguration
import de.jakobschaefer.htma.rendering.HtmaContext
import de.jakobschaefer.htma.rendering.HtmaState
import de.jakobschaefer.htma.webinf.AppManifestPage
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File
import kotlin.io.path.createTempDirectory

class HtmaRouting {
}

class HtmaRoutingBuilder : HtmaRoutingDslBuilder<HtmaRouting> {
  override fun build(): HtmaRouting {
    return HtmaRouting()
  }
}

fun Route.web(spec: HtmaRoutingBuilder.() -> Unit) {
  val configuration = application.htmaConfiguration

  val routing = HtmaRoutingBuilder().apply(spec).build()

  staticResources("/assets", "${configuration.resourceBase}/assets") {
    cacheControl { listOf(CacheControl.MaxAge(maxAgeSeconds = 31_104_000)) }
    preCompressed(CompressedFileType.GZIP)
  }

  setupPageRouting(configuration)
}

private fun Route.setupPageRouting(configuration: HtmaConfiguration) {
  for (page in configuration.appManifest.pages) {
    get(page.remotePath) {
      val pathParameters = call.pathParameters.toMap()
        .mapValues { (_, value) -> value[0] }
      val queryParameters = call.queryParameters.toMap()
      val parameters = pathParameters + queryParameters
      replyHtml(page, configuration, parameters)
    }
    post(page.remotePath) {
      val pathParameters = call.pathParameters.toMap()
        .mapValues { (_, value) -> value[0] }
      val queryParameters = call.queryParameters.toMap()
      val formParameters = mutableMapOf<String, MutableList<String>>()
      call.receiveMultipart().forEachPart { part ->
        val partName = part.name
        if (partName != null) {
          when (part) {
            is PartData.FormItem -> {
              val values = formParameters[partName]
              if (values != null) {
                values.add(part.value)
              } else {
                formParameters[partName] = mutableListOf(part.value)
              }
            }
            is PartData.FileItem -> {
              val fileName = part.originalFileName ?: "upload"
              val file = File.createTempFile("htma-", "-$fileName")
              val filePath = file.absolutePath
              part.provider().copyAndClose(file.writeChannel())
              file.deleteOnExit()
              val values = formParameters[partName]
              if (values != null) {
                values.add(filePath)
              } else {
                formParameters[partName] = mutableListOf(filePath)
              }
            }
            else -> {}
          }
          part.dispose()
        }
      }
      val parameters = pathParameters + queryParameters + formParameters
      replyHtml(page, configuration, parameters)
    }
  }
}

private suspend fun RoutingContext.replyHtml(
  toPage: AppManifestPage,
  configuration: HtmaConfiguration,
  parameters: Map<String, Any>
) {
  val htmaState = HtmaState.build(call, toPage, configuration)
  val htmaContext = HtmaContext(call, htmaState, parameters)
  val responseBody = if (htmaState.isFetchRequest) {
    val outletCssSelector = "#${htmaState.outletSwap!!.oldOutlet.replace(".", "\\.").replace("/", "\\/").replace("$", "\\$")}"
    call.response.header("HX-Retarget", outletCssSelector)
    call.response.header("HX-Reswap", "outerHTML")
    call.response.header("HX-Push-Url", call.request.uri)
    configuration.renderingEngine.renderFragment(htmaState, htmaContext)
  } else {
    configuration.renderingEngine.renderPage(htmaState, htmaContext)
  }
  call.respondText(responseBody, ContentType.Text.Html, HttpStatusCode.OK)
}
