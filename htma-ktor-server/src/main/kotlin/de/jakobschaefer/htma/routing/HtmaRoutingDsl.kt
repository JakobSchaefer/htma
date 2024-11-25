package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.htma
import de.jakobschaefer.htma.respondTemplate
import io.ktor.server.routing.*
import io.ktor.utils.io.*

@KtorDsl
fun Route.web(spec: () -> Unit) {
  val appManifest = application.htma.appManifest
  for (page in appManifest.pages) {
    get(page.remotePath) {
      call.respondTemplate(page.templateName, emptyMap())
    }
  }
}
