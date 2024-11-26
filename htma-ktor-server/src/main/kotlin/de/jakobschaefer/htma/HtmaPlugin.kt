package de.jakobschaefer.htma

import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.application.*
import io.ktor.util.*
import org.thymeleaf.TemplateEngine

internal class HtmaPlugin(
  val config: HtmaPluginConfig,
  val resourceBase: String,
  val templateEngine: TemplateEngine,
  val appManifest: AppManifest,
  val viteManifest: ViteManifest,
)

internal val htmaPluginKey = AttributeKey<HtmaPlugin>("htmaPlugin")

internal val Application.htma: HtmaPlugin
  get() = attributes[htmaPluginKey]

internal fun Application.useHtmaPlugin(plugin: HtmaPlugin) {
  attributes.put(htmaPluginKey, plugin)
}
