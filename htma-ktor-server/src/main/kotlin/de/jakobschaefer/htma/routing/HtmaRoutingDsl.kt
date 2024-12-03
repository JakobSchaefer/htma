package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.htma
import de.jakobschaefer.htma.respondTemplate
import io.ktor.http.*
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
      call.respondTemplate(page.templateName, emptyMap())
    }
    post(page.remotePath) {
      println(call.receiveParameters())
      call.respondTemplate(page.templateName, emptyMap())
    }
  }
}

class HtmaRouting
