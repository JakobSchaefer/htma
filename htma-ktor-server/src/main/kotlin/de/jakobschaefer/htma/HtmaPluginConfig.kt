package de.jakobschaefer.htma

import java.util.*

class HtmaPluginConfig {
  var fallbackLocale: Locale = Locale.getDefault()
  var supportedLocales: List<Locale> = emptyList()
  var resourceBase: String? = null
}
