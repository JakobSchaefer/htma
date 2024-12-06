package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Query
import org.thymeleaf.context.IContext
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

internal class GraphQlExecution(
  val operation: GraphQlOperationRef,
  val context: IContext,
  val services: GraphQlServices,
  val cache: GraphQlExecutionCache,
  private val packageName: String = "de.jakobschaefer.htma"
) {

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
    val kClass = Class.forName("${packageName}.${operation.serviceName}.${operation.operationName}").kotlin
    val args = kClass.primaryConstructor!!
      .parameters
      .map { param -> findConstructorArgument(param, operation) }.toTypedArray()
    return kClass.primaryConstructor!!.call(*args) as T
  }

  private fun findConstructorArgument(param: KParameter, operation: GraphQlOperationRef): Any {
    val value = operation.variables[param.name]
    return when (param.type.classifier) {
      Optional::class -> if (value is List<*>) {
        if (value.size == 0) {
          Optional.absent()
        } else {
          Optional.present(value)
        }
      } else {
        Optional.presentIfNotNull(value)
      }
      else -> value!!
    }
  }
}
