package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlEngine
import java.util.*

class HtmaPluginConfig {
  var fallbackLocale: Locale = Locale.getDefault()
  var supportedLocales: List<Locale> = emptyList()
  var resourceBase: String? = null
  var graphqlServices: Map<String, GraphQlEngine> = emptyMap()
}
