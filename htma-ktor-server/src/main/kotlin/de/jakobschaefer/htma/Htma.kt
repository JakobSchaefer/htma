package de.jakobschaefer.htma

import de.jakobschaefer.htma.messages.HtmaMessageResolver
import de.jakobschaefer.htma.thymeleaf.dialect.HtmaDialect
import de.jakobschaefer.htma.thymeleaf.HtmaLinkBuilder
import de.jakobschaefer.htma.thymeleaf.context.HtmaContext
import de.jakobschaefer.htma.thymeleaf.context.htma
import de.jakobschaefer.htma.thymeleaf.dialect.components.HtmaWebComponentDialect
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.AppManifestPage
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dataloader.DataLoader
import org.thymeleaf.TemplateEngine
import org.thymeleaf.standard.StandardDialect
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import org.thymeleaf.templateresolver.StringTemplateResolver
import java.io.File
import java.util.Locale

val Htma = createApplicationPlugin(name = "Htma", createConfiguration = ::HtmaPluginConfig) {
  val supportedLocales = pluginConfig.supportedLocales
    ?: applicationConfig.propertyOrNull("htma.supportedLocales")?.getList()?.map { Locale.of(it) }
    ?: emptyList()
  val fallbackLocale = pluginConfig.defaultLocale
    ?: applicationConfig.propertyOrNull("htma.fallbackLocale")?.getString()?.let { Locale.of(it) }
    ?: supportedLocales.firstOrNull()
    ?: Locale.getDefault()
  Logs.htma.info("Supporting languages {} with fallback {}", supportedLocales, fallbackLocale)

  val enableLogic = if (pluginConfig.enableLogic != null) {
    pluginConfig.enableLogic!!
  } else if (applicationConfig.propertyOrNull("htma.enableLogic") != null) {
    applicationConfig.property("htma.enableLogic").getString().toBooleanStrict()
  } else {
    false
  }
  Logs.htma.info("Decoubled logic is {}", if (enableLogic) { "enabled" } else { "not enabled" })

  if (application.developmentMode) {
    GlobalScope.launch {
      ProcessBuilder("npx", "vite", "dev")
        .directory(File("."))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectInput(ProcessBuilder.Redirect.INHERIT)
        .start()
    }
  }

  val session = findStringProperty(
    givenValue = pluginConfig.session,
    propertyName = "htma.session",
  )

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
  val templateEngine = setupTemplateEngine(resourceBase, enableLogic, appManifest)

  // Add plugin to the ktor application
  val plugin = HtmaPlugin(
    config = pluginConfig,
    resourceBase = resourceBase,
    templateEngine = templateEngine,
    appManifest = appManifest,
    viteManifest = viteManifest,
    supportedLocales = supportedLocales,
    defaultLocale = fallbackLocale,
    enableLogic = enableLogic,
    session = session,
  )
  application.useHtmaPlugin(plugin)
  Logs.htma.info("Htma plugin started!")
}

private fun PluginBuilder<HtmaPluginConfig>.setupTemplateEngine(
  resourceBase: String,
  enableLogic: Boolean,
  appManifest: AppManifest,
): TemplateEngine {
  val templateEngine = TemplateEngine()
  val templateResolvers = mutableSetOf<ITemplateResolver>()

  // We want to provide some templates with this library.
  // This resolver should look up only specific names in order to improve performance
  val internalTemplates = ClassLoaderTemplateResolver(HtmaPluginConfig::class.java.classLoader).apply {
    prefix = "internal-templates/"
    suffix = ".html"
    templateMode = TemplateMode.HTML
    order = 1
    resolvablePatterns = setOf("__fragment_root")
  }
  templateResolvers.add(internalTemplates)

  // Templates provided by the user
  val webTemplates = if (application.developmentMode) {
    FileTemplateResolver().apply {
      prefix = "web/"
      suffix = ".html"
      templateMode = TemplateMode.HTML
      isCacheable = false
      order = 3
      useDecoupledLogic = enableLogic
    }
  } else {
    ClassLoaderTemplateResolver().apply {
      prefix = "${resourceBase}/web/"
      suffix = ".html"
      templateMode = TemplateMode.HTML
      order = 3
      useDecoupledLogic = enableLogic
    }
  }
  templateResolvers.add(webTemplates)

  val stringTemplateResolver = StringTemplateResolver()
  templateResolvers.add(stringTemplateResolver)

  templateEngine.templateResolvers = templateResolvers
  templateEngine.setLinkBuilder(HtmaLinkBuilder())
  templateEngine.setMessageResolver(HtmaMessageResolver())
  templateEngine.clearDialects()
  templateEngine.addDialect(StandardDialect()) // NOTE: Precedence = 1000
  templateEngine.addDialect(HtmaDialect("Htma", "th"))
  templateEngine.addDialect(HtmaWebComponentDialect("HtmaWebComponents", "th", appManifest.components))
  return templateEngine
}

private fun PluginBuilder<HtmaPluginConfig>.findStringProperty(
  givenValue: String?,
  propertyName: String,
  fallbackValue: String
): String {
  return givenValue ?: (environment.config.propertyOrNull(propertyName)?.getString()) ?: fallbackValue
}

private fun PluginBuilder<HtmaPluginConfig>.findStringProperty(
  givenValue: String?,
  propertyName: String
): String? {
  return givenValue ?: (environment.config.propertyOrNull(propertyName)?.getString())
}

internal suspend fun RoutingCall.replyHtml(
  toPage: AppManifestPage,
  data: Map<String, Any?>
) {
  val context = HtmaContext(this, toPage, data)

  // Render response
  respondText(contentType = ContentType.Text.Html, status = HttpStatusCode.OK) {
    if (context.htma.isHtmxRequest) {
      renderFragment(context)
    } else {
      renderPage(context, toPage)
    }
  }
}

internal fun RoutingCall.renderFragment(context: HtmaContext): String {
  val outletCssSelector = "#${context.htma.outletSwap!!.oldOutlet.replace(".", "\\.").replace("/", "\\/").replace("$", "\\$")}"
  response.header("HX-Retarget", outletCssSelector)
  response.header("HX-Reswap", "outerHTML")

  return application.htma.templateEngine.process("__fragment_root", context)
}

internal fun RoutingCall.renderPage(context: HtmaContext, toPage: AppManifestPage): String {
  return application.htma.templateEngine.process("__root", context)
}
