package de.jakobschaefer.htma

import de.jakobschaefer.htma.messages.HtmaMessageFormatter
import de.jakobschaefer.htma.rendering.HtmaTemplateEngine
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.application.*
import java.util.Locale

val Htma = createApplicationPlugin(
  name = "Htma",
  createConfiguration = ::HtmaKtorPluginConfiguration
) {
  val isInDebugMode = java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("jdwp") >= 0
  Logs.htma.info("Debug mode is {}", if (isInDebugMode) { "enabled" } else { "disabled" })
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

  val messageFormatter = HtmaMessageFormatter()

  val templateEngine = HtmaTemplateEngine(
    isDevelopmentMode = isDevelopmentMode,
    resourceBase = resourceBase,
    appManifest = appManifest,
    viteManifest = viteManifest,
    messageFormatter = messageFormatter
  )

  val plugin = HtmaConfiguration(
    isDevelopmentMode = isDevelopmentMode,
    resourceBase = resourceBase,
    appManifest = appManifest,
    viteManifest = viteManifest,
    supportedLocales = supportedLocales,
    defaultLocale = defaultLocale,
    isLogicEnabled = isLogicEnabled,
    templateEngine = templateEngine,
    formatter = messageFormatter,
    graphQlService = pluginConfig.graphQlService
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
