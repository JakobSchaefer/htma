package de.jakobschaefer.htma

import java.util.*

class HtmaPluginConfig {
  /**
   * The supported locales are used to detect the users language from the 'Accept-Languages' header
   */
  var supportedLocales: List<Locale>? = null
  var defaultLocale: Locale? = null
  var resourceBase: String? = null
  var enableLogic: Boolean? = null
  var session: String? = null
}
