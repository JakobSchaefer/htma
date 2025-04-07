package de.jakobschaefer.htma.rendering

import io.ktor.server.routing.*
import io.ktor.util.*
import org.apache.commons.jexl3.MapContext
import java.util.Locale

class HtmaContext(
  call: RoutingCall,
  htmaState: HtmaState,
  parameters: Map<String, Any?>
) : MapContext() {
  init {
    val acceptLanguageHeader = call.request.headers["Accept-Language"]
    val locale = if (acceptLanguageHeader != null) {
      val acceptedLanguages = Locale.LanguageRange.parse(acceptLanguageHeader)
      Locale.lookup(acceptedLanguages, htmaState.supportedLocales)
        ?: htmaState.defaultLocale
    } else {
      htmaState.defaultLocale
    }
    set("locale", locale)
    set("parameters", parameters)
    set("htma", htmaState)
  }
}
