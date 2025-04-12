package de.jakobschaefer.htma.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.CompletableFuture

internal class GraphQlDataFetcher<T>(
  private val coroutineScope: CoroutineScope,
  private val dataResolver: GraphQlDataResolver<T>
) : DataFetcher<CompletableFuture<T>> {
  override fun get(environment: DataFetchingEnvironment): CompletableFuture<T> {
    return coroutineScope.async {
      dataResolver.resolve(buildEnvironment(environment))
    }.asCompletableFuture()
  }

  private fun buildEnvironment(environment: DataFetchingEnvironment): GraphQlDataResolverEnvironment {
    return GraphQlDataResolverEnvironment(
      arguments = environment.arguments
    )
  }
}
