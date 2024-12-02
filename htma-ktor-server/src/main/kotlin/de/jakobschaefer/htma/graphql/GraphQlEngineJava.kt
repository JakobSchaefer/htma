package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.*
import com.apollographql.apollo.api.json.buildJsonString
import com.apollographql.apollo.api.json.jsonReader
import com.google.gson.GsonBuilder
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaParser
import okio.buffer
import okio.source

class GraphQlEngineJava(
  schemaResourceFile: String,
  runtimeWiring: RuntimeWiring,
) : GraphQlEngine {
    private val schema: GraphQLSchema
    private val gson = GsonBuilder().create()

    init {
      val schemaFile = loadClassPathResourceAsString(schemaResourceFile)
      val typeDefinitions = SchemaParser().parse(schemaFile)
      schema = SchemaGenerator()
        .makeExecutableSchema(typeDefinitions, runtimeWiring)
    }

  @OptIn(ApolloExperimental::class)
  override suspend fun <D : Query.Data> query(query: Query<D>): ApolloResponse<D> {
    val gql =
      GraphQL.newGraphQL(schema)
        .build()
    val input = ExecutionInput.newExecutionInput()
      .operationName(query.name())
      .query(query.document())
      .variables(query.variables(CustomScalarAdapters.Empty).valueMap)
      .build()
    val graphQlResponse = gson.toJson(gql.execute(input).toSpecification())
    return graphQlResponse.byteInputStream().source()
      .buffer()
      .jsonReader()
      .toApolloResponse(
        operation = query,
      )
  }
}

private fun loadClassPathResourceAsString(path: String): String {
  val fixedPath =
    if (path.startsWith("/")) {
      path.substringAfter("/")
    } else {
      path
    }
  return object {}.javaClass.classLoader.getResource(fixedPath).readText()
}
