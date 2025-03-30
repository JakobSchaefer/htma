package de.jakobschaefer.htma.thymeleaf.context

import de.jakobschaefer.htma.htma
import de.jakobschaefer.htma.webinf.AppManifestPage
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.thymeleaf.context.IContext
import java.util.*

class HtmaContext(
  call: RoutingCall,
  fromPage: AppManifestPage?,
  toPage: AppManifestPage,
  data: Map<String, Any?>,
  session: String?
) : IContext {

  private val locale: Locale

  override fun getLocale(): Locale {
    return locale
  }

  init {
    val acceptLanguageHeader = call.request.headers["Accept-Language"]
    locale = if (acceptLanguageHeader != null) {
      val acceptedLanguages = Locale.LanguageRange.parse(acceptLanguageHeader)
      Locale.lookup(acceptedLanguages, call.application.htma.supportedLocales)
        ?: call.application.htma.defaultLocale
    } else {
      call.application.htma.defaultLocale
    }
  }

  private val variables = LinkedHashMap(
    mapOf(
      "htma" to HtmaRenderContext(
        isDevelopment = call.application.developmentMode,
        fromPage = fromPage,
        toPage = toPage,
        vite = call.application.htma.viteManifest,
        app = call.application.htma.appManifest,
        isHtmxRequest = call.request.headers["Hx-Request"] == "true",
        outletSwap = fromPage?.let { HtmaOutletSwap.build(fromPage, toPage) },
      ),
      "param" to call.parameters.toMap(),
      "session" to session?.let { call.sessions.get(session) },
      "data" to data
    )
  )

  override fun containsVariable(name: String): Boolean {
    return variables.containsKey(name)
  }

  override fun getVariableNames(): Set<String> {
    return variables.keys
  }

  override fun getVariable(name: String): Any? {
    return variables[name]
  }

  fun setVariable(name: String, value: Any) {
    variables[name] = value
  }
}

val IContext.htma: HtmaRenderContext
  get() = getVariable("htma") as HtmaRenderContext
