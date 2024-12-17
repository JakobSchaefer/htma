package de.jakobschaefer.htma.graphql

import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import io.ktor.server.routing.*
import kotlinx.coroutines.future.await

class GraphQlEngine(
  private val schema: GraphQLSchema,
  private val contextProvider: suspend RoutingContext.() -> Any
) {

  suspend fun execute(call: RoutingCall, operation: GraphQlOperationRef, query: String): Map<String, Any> {
    val routingContext = RoutingContext(call)
    val ctx = routingContext.contextProvider()
    val graphql = GraphQL.newGraphQL(schema)
      .build()
    val input = ExecutionInput.newExecutionInput()
      .query(query)
      .operationName(operation.operationName)
      .variables(operation.variables)
      .graphQLContext(
        mapOf("ctx" to ctx, "routingContext" to routingContext)
      )
    val result = graphql.executeAsync(input).await()
    return result.toSpecification()
  }
}
