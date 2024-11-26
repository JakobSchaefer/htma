package de.jakobschaefer.htma

import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import org.thymeleaf.context.Context
import org.thymeleaf.context.IContext

data class HtmaRenderContext(
  val isDevelopment: Boolean,
  val vite: ViteManifest,
  val app: AppManifest
) {
  fun updateContext(context: Context) {
    context.setVariable("htma", this)
  }

  companion object {
    fun fromContext(context: IContext) = context.getVariable("htma") as HtmaRenderContext
  }
}
