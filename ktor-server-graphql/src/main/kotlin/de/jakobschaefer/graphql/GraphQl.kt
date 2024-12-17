package de.jakobschaefer.graphql

import com.google.gson.GsonBuilder
import graphql.*
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.*

class GraphQlConfig(
  var schemaFile: String? = null
)

val GraphQl =
    createApplicationPlugin("GraphQl", createConfiguration = ::GraphQlConfig) {
      val schemaFilePath = pluginConfig.schemaFile
        ?: environment.config.propertyOrNull("graphql.schemaFile")?.getString()
        ?: "/graphql/schema.graphqls"
      val schemaFile = loadClassPathResourceAsString(schemaFilePath)
      val typeDefinitions = SchemaParser().parse(schemaFile)
      application.attributes.put(typeDefinitionKey, typeDefinitions)
    }

private val typeDefinitionKey = AttributeKey<TypeDefinitionRegistry>("typeDefinitionRegistry")
private val Application.typeDefinitions: TypeDefinitionRegistry
  get() = attributes[typeDefinitionKey]

private val gson = GsonBuilder().create()

@KtorDsl
fun Route.graphql(path: String, spec: GraphQlSchemaWiring.() -> Unit) {
  val schemaWiring = GraphQlSchemaWiring()
  schemaWiring.spec()
  schemaWiring.runtimeWiring
  val schema =
      SchemaGenerator()
          .makeExecutableSchema(application.typeDefinitions, schemaWiring.runtimeWiring.build())
  post(path) {
    val request = call.receive<GraphQlRequest>()
    val gql =
        GraphQL.newGraphQL(schema)
            .defaultDataFetcherExceptionHandler(GraphQlExceptionHandler())
            .build()
    val result = coroutineScope {
      val input =
          ExecutionInput.newExecutionInput()
              .graphQLContext(
                  mapOf("coroutineScope" to this@coroutineScope, "routingContext" to this@post))
              .query(request.query)
      if (request.operationName != null) {
        input.operationName(request.operationName)
      }
      if (request.variables != null) {
        input.variables(jsonObjectToMap(request.variables))
      }

      gql.executeAsync(input).await()
    }
    val responseData = result.toSpecification()
    call.respondText(gson.toJson(responseData), contentType = ContentType.Application.Json)
  }
}

private fun jsonObjectToMap(obj: JsonObject): Map<String, Any> {
  return jsonElementToMap(obj) as Map<String, Any>
}

private fun jsonElementToMap(el: JsonElement): Any? {
  return when (el) {
    is JsonObject -> el.toMap().mapValues { entry -> jsonElementToMap(entry.value) }
    is JsonArray -> el.map { jsonElementToMap(it) }
    is JsonNull -> null
    is JsonPrimitive -> (el.booleanOrNull ?: el.content)
  }
}

class GraphQlExecutionException(message: String) : RuntimeException(message)

fun loadClassPathResourceAsString(path: String): String {
  val fixedPath =
    if (path.startsWith("/")) {
      path.substringAfter("/")
    } else {
      path
    }
  return object {}.javaClass.classLoader.getResource(fixedPath).readText()
}

