package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlExecutionCache
import de.jakobschaefer.htma.routing.HtmaClientNavigationContext
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

val Htma = createApplicationPlugin(name = "Htma", createConfiguration = ::HtmaPluginConfig) {
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

  // Templates provided by the user
  val webTemplates = if (application.developmentMode) {
    FileTemplateResolver().apply {
      prefix = "web/"
      suffix = ".html"
      templateMode = TemplateMode.HTML
      isCacheable = false
      order = 2
    }
  } else {
    ClassLoaderTemplateResolver().apply {
      prefix = "${resourceBase}/web/"
      suffix = ".html"
      templateMode = TemplateMode.HTML
      order = 2
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

suspend fun ApplicationCall.respondTemplate(
  templateName: String,
  data: Map<String, Any?> = emptyMap(),
  clientContext: HtmaClientNavigationContext? = null
) {
  val graphqlExecutionCache = GraphQlExecutionCache(
    entries = ConcurrentHashMap()
  )
  respondText(contentType = ContentType.Text.Html, status = HttpStatusCode.OK) {
    // Detect client language
    val acceptLanguageHeader = request.headers["Accept-Language"]
    val locale =
      if (acceptLanguageHeader != null) {
        val acceptedLanguages = Locale.LanguageRange.parse(acceptLanguageHeader)
        Locale.lookup(acceptedLanguages, application.htma.config.supportedLocales)
          ?: application.htma.config.fallbackLocale
      } else {
        application.htma.config.fallbackLocale
      }

    val renderContext = Context(locale, data)

    // Add htma data to the context
    val htmaRenderContext = HtmaRenderContext(
      isDevelopment = application.developmentMode,
      vite = application.htma.viteManifest,
      app = application.htma.appManifest,
      clientContext = clientContext,
      graphql = graphqlExecutionCache,
      graphqlServices = application.htma.config.graphqlServices,
    )
    htmaRenderContext.updateContext(renderContext)

    // Process template and respond
    application.htma.templateEngine.process(templateName, renderContext)
  }
}
