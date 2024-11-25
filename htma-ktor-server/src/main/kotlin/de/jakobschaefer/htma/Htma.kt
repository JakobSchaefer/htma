package de.jakobschaefer.htma

import de.jakobschaefer.htma.webinf.app.AppManifest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver

val Htma = createApplicationPlugin(name = "Htma", createConfiguration = ::HtmaPluginConfig) {
  val resourceBase = findStringProperty(
    givenValue = pluginConfig.resourceBase,
    propertyName = "htma.resourceBase",
    fallbackValue = "/WEB-INF"
  )
  val appManifest = AppManifest.loadFromResources(
    resourceBase = resourceBase
  )
  val templateEngine = setupTemplateEngine(resourceBase)
  val plugin = HtmaPlugin(
    config = pluginConfig,
    templateEngine = templateEngine,
    appManifest = appManifest
  )
  application.useHtmaPlugin(plugin)
  Logs.htma.info("Htma plugin started!")
}

private fun setupTemplateEngine(resourceBase: String): TemplateEngine {
  val templateEngine = TemplateEngine()
  val templateResolvers = mutableSetOf<ITemplateResolver>()

  // We want to provide some templates with this library.
  // This resolver should look up only specific names in order to improve performance
  val internalTemplates = ClassLoaderTemplateResolver(HtmaPluginConfig::class.java.classLoader).apply {
    prefix = "internal-templates/"
    suffix = ".html"
    templateMode = TemplateMode.HTML
    order = 1
    resolvablePatterns = setOf("base")
  }
  templateResolvers.add(internalTemplates)

  // Templates provided by the user
  val webTemplates = ClassLoaderTemplateResolver().apply {
    prefix = "${resourceBase}/web/"
    suffix = ".html"
    templateMode = TemplateMode.HTML
    order = 2
  }
  templateResolvers.add(webTemplates)

  templateEngine.templateResolvers = templateResolvers
  return templateEngine
}

private fun PluginBuilder<HtmaPluginConfig>.findStringProperty(
  givenValue: String?,
  propertyName: String,
  fallbackValue: String
): String {
  return givenValue ?: (environment.config.propertyOrNull(propertyName)?.getString()) ?: fallbackValue
}

suspend fun ApplicationCall.respondTemplate(templateName: String, data: Map<String, Any?>) {
  respondText(contentType = ContentType.Text.Html, status = HttpStatusCode.OK) {
    val ctx = Context()
    application.htma.templateEngine.process(templateName, ctx)
  }
}
