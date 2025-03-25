package de.jakobschaefer.htma

import de.jakobschaefer.htma.thymeleaf.HtmaContext
import de.jakobschaefer.htma.thymeleaf.htma
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.AppManifestPage
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.thymeleaf.TemplateEngine
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
  val fallbackLocale = pluginConfig.fallbackLocale
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
  val templateEngine = setupTemplateEngine(resourceBase, enableLogic)

  // Add plugin to the ktor application
  val plugin = HtmaPlugin(
    config = pluginConfig,
    resourceBase = resourceBase,
    templateEngine = templateEngine,
    appManifest = appManifest,
    viteManifest = viteManifest,
    supportedLocales = supportedLocales,
    fallbackLocale = fallbackLocale,
    enableLogic = enableLogic
  )
  application.useHtmaPlugin(plugin)
  Logs.htma.info("Htma plugin started!")
}

private fun PluginBuilder<HtmaPluginConfig>.setupTemplateEngine(resourceBase: String, enableLogic: Boolean): TemplateEngine {
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

internal suspend fun RoutingCall.replyHtml(
  toPage: AppManifestPage
) {
  val isHtmxRequest = request.headers["Hx-Request"] == "true"
  val fromPage = if (isHtmxRequest) {
    val fromPathSegments = Url(request.headers["Hx-Current-Url"]!!).segments
    application.htma.appManifest.pages
      .find {
        val remotePathSegments = if (it.remotePath == "/") {
          emptyList()
        } else {
          it.remotePath.substring(1).split("/")
        }
        if (fromPathSegments.size != remotePathSegments.size) {
          return@find false
        }
        for (i in fromPathSegments.indices) {
          if (fromPathSegments[i] != remotePathSegments[i] && !remotePathSegments[i].startsWith('{')) {
            return@find false
          }
        }
        return@find true
      }!!
  } else {
    null
  }
  val context = HtmaContext(this, fromPage, toPage)

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
  // Detect the common layout of fromPage and toPage
  val outletCssSelector = "#${context.htma.outletSwap!!.oldOutlet.replace(".", "\\.").replace("/", "\\/").replace("$", "\\$")}"
  response.header("HX-Retarget", outletCssSelector)
  response.header("HX-Reswap", "outerHTML")

  // Render
  return application.htma.templateEngine.process("__fragment_root", context)
}

internal fun RoutingCall.renderPage(context: HtmaContext, toPage: AppManifestPage): String {
  // Render
  return application.htma.templateEngine.process("__root", context)
}
