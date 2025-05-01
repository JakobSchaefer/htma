package de.jakobschaefer.htma

import de.jakobschaefer.htma.messages.HtmaFormatter
import de.jakobschaefer.htma.rendering.HtmaRenderingEngine
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.http.Cookie
import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.UUID

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

  val sessionIdCookieName = loadConfigProperty(
    input = pluginConfig.sessionIdCookieName,
    propertyName = "htma.sessionIdCookieName",
    mapFn = { it }
  ) ?: "sessionId"
  Logs.htma.info("Using session id cookie name '{}'", sessionIdCookieName)

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

  val formatter = HtmaFormatter()

  val renderingEngine = HtmaRenderingEngine(
    isDevelopmentMode = isDevelopmentMode,
    resourceBase = resourceBase,
    appManifest = appManifest,
    formatter = formatter,
    defaultLocale = defaultLocale,
  )

  val plugin = HtmaConfiguration(
    isDevelopmentMode = isDevelopmentMode,
    resourceBase = resourceBase,
    appManifest = appManifest,
    viteManifest = viteManifest,
    supportedLocales = supportedLocales,
    defaultLocale = defaultLocale,
    isLogicEnabled = isLogicEnabled,
    renderingEngine = renderingEngine,
    formatter = formatter,
    graphQlService = pluginConfig.graphQlService
  )

  onCall {
    val sid = it.request.cookies[sessionIdCookieName]
    val sessionId = if (sid == null) {
      val sessionId = UUID.randomUUID().toString()
      val cookie = Cookie(
        name = sessionIdCookieName,
        value = sessionId,
        httpOnly = true,
        secure = true,
        path = "/",
        extensions = mapOf("SameSite" to "Strict")
      )
      it.response.cookies.append(cookie)
      sessionId
    } else {
      sid
    }
    it.attributes.put(SessionIdAttribute, sessionId)
  }

  application.installHtmaConfiguration(plugin)
  Logs.htma.info("Htma plugin has been configured!")
}

internal val SessionIdAttribute = AttributeKey<String>("SessionId")

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
