package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlService
import de.jakobschaefer.htma.messages.HtmaMessageFormatter
import de.jakobschaefer.htma.rendering.HtmaTemplateEngine
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.application.*
import io.ktor.util.*
import java.util.*

internal class HtmaConfiguration(
  val isDevelopmentMode: Boolean,
  val resourceBase: String,
  val appManifest: AppManifest,
  val viteManifest: ViteManifest,
  val supportedLocales: List<Locale>,
  val defaultLocale: Locale,
  val isLogicEnabled: Boolean,
  val templateEngine: HtmaTemplateEngine,
  val formatter: HtmaMessageFormatter,
  val graphQlService: GraphQlService?
)

internal val htmaConfigurationKey = AttributeKey<HtmaConfiguration>("htma")

internal val Application.htmaConfiguration: HtmaConfiguration
  get() = attributes[htmaConfigurationKey]

internal fun Application.installHtmaConfiguration(plugin: HtmaConfiguration) {
  attributes.put(htmaConfigurationKey, plugin)
}
