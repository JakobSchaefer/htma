package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.*
import com.apollographql.apollo.api.json.jsonReader
import com.google.gson.GsonBuilder
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaParser
import kotlinx.coroutines.future.await
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

  override suspend fun <D : Query.Data> query(query: Query<D>): ApolloResponse<D> {
    val input = ExecutionInput.newExecutionInput()
        .operationName(query.name())
        .query(query.document())
        .variables(query.variables(CustomScalarAdapters.Empty).valueMap)
        .build()
    val graphQlResponse = execute(input)
    val jsonReader = graphQlResponse.byteInputStream().source()
      .buffer()
      .jsonReader()
    return query.parseResponse(jsonReader)
  }

  override suspend fun <D : Mutation.Data> mutate(mutation: Mutation<D>): ApolloResponse<D> {
    val input = ExecutionInput.newExecutionInput()
      .operationName(mutation.name())
      .query(mutation.document())
      .variables(mutation.variables(CustomScalarAdapters.Empty).valueMap)
      .build()
    val graphQlResponse = execute(input)
    val jsonReader = graphQlResponse.byteInputStream().source().buffer().jsonReader()
    return mutation.parseResponse(jsonReader)
  }

  private suspend fun execute(input: ExecutionInput): String {
    val gql =
      GraphQL.newGraphQL(schema)
        .build()
    val result = gql.executeAsync(input).await()
    return gson.toJson(result.toSpecification())
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
