package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.htmaConfiguration
import de.jakobschaefer.htma.rendering.HtmaContext
import de.jakobschaefer.htma.rendering.HtmaState
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
      val htmaState = HtmaState.build(call, page, configuration)
      val htmaContext = HtmaContext(call, htmaState)
      val responseBody = if (htmaState.isFetchRequest) {
        val outletCssSelector = "#${htmaState.outletSwap!!.oldOutlet.replace(".", "\\.").replace("/", "\\/").replace("$", "\\$")}"
        call.response.header("HX-Retarget", outletCssSelector)
        call.response.header("HX-Reswap", "outerHTML")
        configuration.renderingEngine.renderFragment(htmaState, htmaContext)
      } else {
        configuration.renderingEngine.renderPage(htmaState, htmaContext)
      }
      call.respondText(responseBody, ContentType.Text.Html, HttpStatusCode.OK)
    }
  }
}
