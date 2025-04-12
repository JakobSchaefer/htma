package de.jakobschaefer.htma.graphql

import graphql.GraphQL
import graphql.schema.GraphQLSchema
import kotlinx.coroutines.future.await

internal class GraphQlServiceImpl(
  private val graphQlSchema: GraphQLSchema
) : GraphQlService {

  override suspend fun execute(request: GraphQlRequest): Map<String, Any> {
    val graphql = GraphQL.newGraphQL(graphQlSchema)
      .build()

    val reply = graphql.executeAsync { inputBuilder ->
      inputBuilder.query(request.query)
      if (request.operationName != null) {
        inputBuilder.operationName(request.operationName)
      }
      inputBuilder.graphQLContext(
        mapOf("to" to "")
      ).variables(request.variables)
    }.await()

    return reply.toSpecification()
  }
}
