package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Query
import com.google.gson.GsonBuilder
import org.thymeleaf.context.IContext

internal class GraphQlExecution(
  val operation: GraphQlOperationRef,
  val context: IContext,
  val services: GraphQlServices,
  val cache: GraphQlExecutionCache,
  private val packageName: String = "de.jakobschaefer.htma"
) {
  private val gson = GsonBuilder()
    .registerTypeAdapter(Optional::class.java, ApolloOptionalTypeAdapter<Any>())
    .create()

  suspend fun executeQueryAndCache(): GraphQlResponse {
    val query = buildOperation<Query<*>>()
    val engine = services[operation.serviceName] ?: throw IllegalStateException("Unknown GraphQL service ${operation.serviceName}")
    val response = engine.query(query = query).toGraphQlResponse()
    cache.entries[operation] = response
    return response
  }

  suspend fun executeMutationAndCache(): GraphQlResponse {
    val mutation = buildOperation<Mutation<*>>()
    val engine = services[operation.serviceName] ?: throw IllegalStateException("Unknown GraphQL service ${operation.serviceName}")
    val response = engine.mutate(mutation).toGraphQlResponse()
    cache.entries[operation] = response
    return response
  }

  @Suppress("UNCHECKED_CAST")
  fun <T> buildOperation(): T {
    val javaClass = Class.forName("${packageName}.${operation.serviceName}.${operation.operationName}") as Class<out T>
    val args = gson.toJson(operation.variables)
    val operation = gson.fromJson(args, javaClass)
    return operation
  }
}
