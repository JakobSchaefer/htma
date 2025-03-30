package de.jakobschaefer.htma

import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.application.*
import io.ktor.util.*
import org.thymeleaf.TemplateEngine
import java.util.*

internal class HtmaPlugin(
  val config: HtmaPluginConfig,
  val resourceBase: String,
  val templateEngine: TemplateEngine,
  val appManifest: AppManifest,
  val viteManifest: ViteManifest,
  val supportedLocales: List<Locale>,
  val defaultLocale: Locale,
  val enableLogic: Boolean,
  val session: String?
)

internal val htmaPluginKey = AttributeKey<HtmaPlugin>("htma")

internal val Application.htma: HtmaPlugin
  get() = attributes[htmaPluginKey]

internal fun Application.useHtmaPlugin(plugin: HtmaPlugin) {
  attributes.put(htmaPluginKey, plugin)
}
