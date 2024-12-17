package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlEngine
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
  var graphqlEngine: GraphQlEngine?
)

internal val htmaPluginKey = AttributeKey<HtmaPlugin>("htma")

internal val Application.htma: HtmaPlugin
  get() = attributes[htmaPluginKey]

internal fun Application.useHtmaPlugin(plugin: HtmaPlugin) {
  attributes.put(htmaPluginKey, plugin)
}
