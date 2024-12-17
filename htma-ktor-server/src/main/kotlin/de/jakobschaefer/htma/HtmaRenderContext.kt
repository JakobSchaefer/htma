package de.jakobschaefer.htma

import de.jakobschaefer.htma.routing.HtmaNavigationClientContext
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.routing.*
import org.thymeleaf.context.IContext
import org.thymeleaf.context.WebContext

data class HtmaRenderContext(
  val isDevelopment: Boolean,
  val vite: ViteManifest,
  val app: AppManifest,
  val call: RoutingCall,
  val clientContext: HtmaNavigationClientContext?
) {
  fun updateContext(context: WebContext) {
    context.setVariable("htma", this)
  }

  companion object {
    fun fromContext(context: IContext) = context.getVariable("htma") as HtmaRenderContext
  }
}
