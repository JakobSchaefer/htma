package de.jakobschaefer.htma

import de.jakobschaefer.htma.rendering.HtmaRenderingEngine
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

val Htma = createApplicationPlugin(
  name = "Htma",
  createConfiguration = ::HtmaKtorPluginConfiguration
) {
  val isInDebugMode = java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("jdwp") >= 0
  val isDevelopmentMode = isInDebugMode || application.developmentMode
  Logs.htma.info("Htma installation has been started in {} mode", if (isDevelopmentMode) { "DEVELOPMENT" } else { "PRODUCTION" })

  val supportedLocales = loadConfigProperty(
    input = pluginConfig.supportedLocales,
    propertyName = "htma.supportedLocales",
    mapFn = { Locale.forLanguageTag(it) },
  ) ?: emptyList()
  val defaultLocale = loadConfigProperty(
    input = pluginConfig.defaultLocale,
    propertyName = "htma.defaultLocale",
    mapFn = { Locale.forLanguageTag(it) },
  ) ?: Locale.getDefault()
  Logs.htma.info("Supporting locales {} with default {}", supportedLocales, defaultLocale)

  val isLogicEnabled = loadConfigProperty(
    input = pluginConfig.isLogicEnabled,
    propertyName = "htma.isLogicEnabled",
    mapFn = { it.toBooleanStrict() },
  ) ?: false
  Logs.htma.info("Logic is {}", if (isLogicEnabled) { "enabled" } else { "disabled" })

  val session = loadConfigProperty(
    input = pluginConfig.session,
    propertyName = "htma.session",
    mapFn = { it }
  )
  Logs.htma.info("Using session name '{}'", session)

  // Read manifest files
  val resourceBase = loadConfigProperty(
    input = pluginConfig.resourceBase,
    propertyName = "htma.resourceBase",
    mapFn = { it }
  ) ?: "/WEB-INF"
  Logs.htma.info("Loading resource from '{}'", resourceBase)

  val appManifest = AppManifest.loadFromResources(
    resourceBase = resourceBase
  )
  Logs.htma.info("App manifest loaded:\n{}", JSON.encodeToString(appManifest))

  val viteManifest = if (isDevelopmentMode) {
    ViteManifest.development()
  } else {
    ViteManifest.loadFromResources(
      resourceBase = resourceBase
    )
  }
  Logs.htma.info("Vite manifest loaded:\n{}", JSON.encodeToString(viteManifest))

  if (isDevelopmentMode) {
    Logs.htma.info("Starting vite dev server...")
    GlobalScope.launch {
      ProcessBuilder("npx", "vite", "dev")
        .directory(File("."))
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectInput(ProcessBuilder.Redirect.INHERIT)
        .start()
    }
  }

  val renderingEngine = HtmaRenderingEngine(
    isDevelopmentMode = isDevelopmentMode,
    resourceBase = resourceBase,
    appManifest = appManifest,
  )

  val plugin = HtmaConfiguration(
    isDevelopmentMode = isDevelopmentMode,
    resourceBase = resourceBase,
    appManifest = appManifest,
    viteManifest = viteManifest,
    supportedLocales = supportedLocales,
    defaultLocale = defaultLocale,
    isLogicEnabled = isLogicEnabled,
    session = session,
    renderingEngine = renderingEngine,
  )
  application.installHtmaConfiguration(plugin)
  Logs.htma.info("Htma plugin has been configured!")
}

private fun <P: Any, T> PluginBuilder<P>.loadConfigProperty(input: List<T>?, propertyName: String, mapFn: (String) -> T): List<T>? {
  return input ?: applicationConfig.propertyOrNull(propertyName)?.getList()?.map(mapFn)
}

private fun <P: Any, T> PluginBuilder<P>.loadConfigProperty(input: T?, propertyName: String, mapFn: (String) -> T): T? {
  return input ?: applicationConfig.propertyOrNull(propertyName)?.getString()?.let(mapFn)
}

//internal suspend fun <C> RoutingCall.replyHtml(
//  toPage: AppManifestPage,
//  data: Map<String, Any?>,
//  requestContext: C
//) {
//  val context = HtmaContext(this, toPage, data, requestContext)
//
//  // Render response
//  respondText(contentType = ContentType.Text.Html, status = HttpStatusCode.OK) {
//    val state = context.getHtmaState<C>()
//    if (state.isHtmxRequest) {
//      renderFragment(context)
//    } else {
//      renderPage(context)
//    }
//  }
//}
//
//internal fun <C> RoutingCall.renderFragment(context: HtmaContext<C>): String {
//  val outletCssSelector = "#${context.getHtmaState<C>().outletSwap!!.oldOutlet.replace(".", "\\.").replace("/", "\\/").replace("$", "\\$")}"
//  response.header("HX-Retarget", outletCssSelector)
//  response.header("HX-Reswap", "outerHTML")
//
//  return application.htma.templateEngine.process("__fragment_root", context)
//}
//
//internal fun <C> RoutingCall.renderPage(context: HtmaContext<C>): String {
//  return application.htma.templateEngine.process("__root", context)
//}
