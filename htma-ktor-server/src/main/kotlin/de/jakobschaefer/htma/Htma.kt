package de.jakobschaefer.htma

import de.jakobschaefer.htma.thymeleaf.KtorWebExchange
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.WebContext
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.File
import java.util.*

val Htma = createApplicationPlugin(name = "Htma", createConfiguration = ::HtmaPluginConfig) {
  Logs.htma.info("Supporting languages {} with fallack {}", pluginConfig.supportedLocales, pluginConfig.fallbackLocale)
  if (application.developmentMode) {
    GlobalScope.launch {
      ProcessBuilder("npx", "vite", "dev")
        .directory(File("."))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectInput(ProcessBuilder.Redirect.INHERIT)
        .start()
    }
  }

  // Read manifest files
  val resourceBase = findStringProperty(
    givenValue = pluginConfig.resourceBase,
    propertyName = "htma.resourceBase",
    fallbackValue = "/WEB-INF"
  )
  val appManifest = AppManifest.loadFromResources(
    resourceBase = resourceBase
  )
  val viteManifest = if (application.developmentMode) {
    ViteManifest.development()
  } else {
    ViteManifest.loadFromResources(
      resourceBase = resourceBase
    )
  }

  // Setup template engine
  val templateEngine = setupTemplateEngine(resourceBase)

  // Add plugin to the ktor application
  val plugin = HtmaPlugin(
    config = pluginConfig,
    resourceBase = resourceBase,
    templateEngine = templateEngine,
    appManifest = appManifest,
    viteManifest = viteManifest,
    graphqlEngine = null
  )
  application.useHtmaPlugin(plugin)
  Logs.htma.info("Htma plugin started!")
}

private fun PluginBuilder<HtmaPluginConfig>.setupTemplateEngine(resourceBase: String): TemplateEngine {
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

  val graphqlTemplates = if (application.developmentMode) {
    FileTemplateResolver().apply {
      prefix = "web/"
      suffix = ""
      templateMode = TemplateMode.TEXT
      isCacheable = false
      order = 2
      resolvablePatterns = setOf("*.graphql")
    }
  } else {
    ClassLoaderTemplateResolver().apply {
      prefix = "${resourceBase}/web/"
      suffix = ""
      templateMode = TemplateMode.TEXT
      order = 2
      resolvablePatterns = setOf("*.graphql")
    }
  }
  templateResolvers.add(graphqlTemplates)

  // Templates provided by the user
  val webTemplates = if (application.developmentMode) {
    FileTemplateResolver().apply {
      prefix = "web/"
      suffix = ".html"
      templateMode = TemplateMode.HTML
      isCacheable = false
      order = 3
    }
  } else {
    ClassLoaderTemplateResolver().apply {
      prefix = "${resourceBase}/web/"
      suffix = ".html"
      templateMode = TemplateMode.HTML
      order = 3
    }
  }
  templateResolvers.add(webTemplates)

  templateEngine.templateResolvers = templateResolvers
  templateEngine.setLinkBuilder(HtmaLinkBuilder())
  templateEngine.setMessageResolver(HtmaMessageResolver())
  templateEngine.addDialect(HtmaDialect())
  return templateEngine
}

private fun PluginBuilder<HtmaPluginConfig>.findStringProperty(
  givenValue: String?,
  propertyName: String,
  fallbackValue: String
): String {
  return givenValue ?: (environment.config.propertyOrNull(propertyName)?.getString()) ?: fallbackValue
}

suspend fun RoutingCall.respondTemplate(
  templateName: String,
  data: Map<String, Any> = emptyMap()
) {
  val htmaContext = HtmaRenderContext(
    isDevelopment = application.developmentMode,
    vite = application.htma.viteManifest,
    app = application.htma.appManifest,
    call = this,
  )

  // build thymeleaf's web context
  val webContext = buildWebContext(data)

  // Add HTMA data to web context
  htmaContext.updateContext(webContext)

  respondText(contentType = ContentType.Text.Html, status = HttpStatusCode.OK) {
    application.htma.templateEngine.process(templateName, webContext)
  }
}

internal fun RoutingCall.buildWebContext(data: Map<String, Any>): WebContext {
  val acceptLanguageHeader = request.headers["Accept-Language"]
  val locale =
    if (acceptLanguageHeader != null) {
      val acceptedLanguages = Locale.LanguageRange.parse(acceptLanguageHeader)
      Locale.lookup(acceptedLanguages, application.htma.config.supportedLocales)
        ?: application.htma.config.fallbackLocale
    } else {
      application.htma.config.fallbackLocale
    }
  return WebContext(KtorWebExchange(this), locale, data)
}
