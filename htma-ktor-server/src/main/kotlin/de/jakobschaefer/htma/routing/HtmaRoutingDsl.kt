package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.htma
import de.jakobschaefer.htma.respondTemplate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

@KtorDsl
fun Route.htma(spec: HtmaRouting.() -> Unit) {
  val appManifest = application.htma.appManifest
  val resourceBase = application.htma.resourceBase
  staticResources("/assets", "$resourceBase/assets") {
    cacheControl { listOf(CacheControl.MaxAge(maxAgeSeconds = 31_104_000)) }
    preCompressed(CompressedFileType.GZIP)
  }
  for (page in appManifest.pages) {
    get(page.remotePath) {
      val parameters = call.queryParameters
      val clientContext = call.ifHtmxRequest {
        HtmaClientNavigationContext.fromParameters(parameters)
      }
      call.respondTemplate(page.templateName, emptyMap(), clientContext)
    }
    post(page.remotePath) {
      val parameters = call.receiveParameters()
      val clientContext = call.ifHtmxRequest {
        HtmaClientNavigationContext.fromParameters(parameters)
      }
      call.respondTemplate(page.templateName, emptyMap(), clientContext)
    }
  }
}

class HtmaRouting

private inline fun <T> ApplicationCall.ifHtmxRequest(block: () -> T): T? {
  val isHxRequest = request.headers["Hx-Request"]?.toBoolean() ?: false
  return if (isHxRequest) {
    block()
  } else {
    null
  }
}
