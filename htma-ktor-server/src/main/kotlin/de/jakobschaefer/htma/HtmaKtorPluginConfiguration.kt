package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlDsl
import de.jakobschaefer.htma.graphql.GraphQlService
import de.jakobschaefer.htma.graphql.GraphQlServiceBuilder
import java.util.*

class HtmaKtorPluginConfiguration {
  var supportedLocales: List<Locale>? = null
  var defaultLocale: Locale? = null
  var resourceBase: String? = null
  var isLogicEnabled: Boolean? = null
  var graphQlService: GraphQlService? = null
  var sessionIdCookieName: String? = null

  @GraphQlDsl
  fun graphql(
    schemaResource: String = "graphql/schema.graphqls",
    spec: GraphQlServiceBuilder.() -> Unit
  ) {
    val schema = loadAppResource(schemaResource).readAllBytes().toString(Charsets.UTF_8)
    graphQlService = GraphQlServiceBuilder(schema).apply(spec).build()
  }
}
