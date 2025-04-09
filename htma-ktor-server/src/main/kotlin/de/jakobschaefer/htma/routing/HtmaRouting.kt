package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.htmaConfiguration
import de.jakobschaefer.htma.rendering.jexl.HtmaContext
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
import java.util.*

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
      val pathParams = call.pathParameters.toMap()
      val queryParams = call.queryParameters.toMap()
      val params = pathParams + queryParams
      replyHtml(page, configuration, params)
    }
    post(page.remotePath) {
      val pathParams = call.pathParameters.toMap()
      val queryParams = call.queryParameters.toMap()
      val formParams = call.receiveFormParams()
      val params = pathParams + queryParams + formParams
      replyHtml(page, configuration, params)
    }
  }
}

private suspend fun RoutingContext.replyHtml(
  toPage: AppManifestPage,
  configuration: HtmaConfiguration,
  params: HtmaParams
) {
  val htmaState = HtmaState.build(call, toPage, configuration)
  val htmaContext = HtmaContext(
    call = call,
    locale = detectUserLocale(htmaState),
    htmaState = htmaState,
    params = params,
    configuration = configuration
  )
  val responseBody = if (htmaState.isFetchRequest) {
    val outletCssSelector =
      "#${htmaState.outletSwap!!.oldOutlet.replace(".", "\\.").replace("/", "\\/").replace("$", "\\$")}"
    call.response.header("HX-Retarget", outletCssSelector)
    call.response.header("HX-Reswap", "outerHTML")
    call.response.header("HX-Push-Url", call.request.uri)
    configuration.renderingEngine.renderFragment(htmaState, htmaContext)
  } else {
    configuration.renderingEngine.renderPage(htmaState, htmaContext)
  }
  call.respondText(responseBody, ContentType.Text.Html, HttpStatusCode.OK)
}

private fun RoutingContext.detectUserLocale(htmaState: HtmaState): Locale {
  val acceptLanguageHeader = call.request.headers["Accept-Language"]
  val locale = if (acceptLanguageHeader != null) {
    val acceptedLanguages = Locale.LanguageRange.parse(acceptLanguageHeader)
    Locale.lookup(acceptedLanguages, htmaState.supportedLocales)
      ?: htmaState.defaultLocale
  } else {
    htmaState.defaultLocale
  }
  return locale
}
private suspend fun RoutingCall.receiveFormParams(): HtmaParams {
  return if (
    request.contentType().contentType == ContentType.MultiPart.FormData.contentType
  ) {
    val foundParams = mutableMapOf<String, MutableList<String>>()
    receiveMultipart().forEachPart { part ->
      val partName = part.name
      if (partName != null) {
        when (part) {
          is PartData.FormItem -> {
            val values = foundParams[partName]
            if (values != null) {
              values.add(part.value)
            } else {
              foundParams[partName] = mutableListOf(part.value)
            }
          }

          is PartData.FileItem -> {
            val fileName = part.originalFileName ?: "upload"
            val file = File.createTempFile("htma-", "-$fileName")
            val filePath = file.absolutePath
            part.provider().copyAndClose(file.writeChannel())
            file.deleteOnExit()
            val values = foundParams[partName]
            if (values != null) {
              values.add(filePath)
            } else {
              foundParams[partName] = mutableListOf(filePath)
            }
          }

          else -> {}
        }
        part.dispose()
      }
    }
    foundParams
  } else {
    receiveParameters().toMap()
  }
}
