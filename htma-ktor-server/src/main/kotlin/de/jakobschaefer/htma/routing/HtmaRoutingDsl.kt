package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.graphql.GraphQlEngine
import de.jakobschaefer.htma.htma
import de.jakobschaefer.htma.respondTemplate
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.TypeDefinitionRegistry
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*

@KtorDsl
fun Route.htma(spec: HtmaRouting.() -> Unit) {
  val appManifest = application.htma.appManifest
  val resourceBase = application.htma.resourceBase

  val routingSpec = HtmaRouting().apply(spec)
  application.htma.graphqlEngine = routingSpec.engine

  staticResources("/assets", "$resourceBase/assets") {
    cacheControl { listOf(CacheControl.MaxAge(maxAgeSeconds = 31_104_000)) }
    preCompressed(CompressedFileType.GZIP)
  }

  for (page in appManifest.pages) {
    get(page.remotePath) {
      call.respondTemplate(page.templateName, emptyMap())
    }
  }
}

class HtmaRouting {
  var engine: GraphQlEngine? = null

  @KtorDsl
  fun graphql(typeDefinitions: TypeDefinitionRegistry, contextProvider: suspend RoutingContext.() -> Any = {}, runtimeWiring: () -> RuntimeWiring) {
    val wiring = runtimeWiring()
    val schema = SchemaGenerator().makeExecutableSchema(typeDefinitions, wiring)
    engine = GraphQlEngine(schema, contextProvider)
  }
}
