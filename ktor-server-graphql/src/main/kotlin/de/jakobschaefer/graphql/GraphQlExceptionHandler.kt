package de.jakobschaefer.graphql

import graphql.ExceptionWhileDataFetching
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import java.util.concurrent.CompletableFuture

class GraphQlExceptionHandler : DataFetcherExceptionHandler {
  override fun handleException(
      handlerParameters: DataFetcherExceptionHandlerParameters
  ): CompletableFuture<DataFetcherExceptionHandlerResult> {
    val exception = handlerParameters.exception
    val sourceLocation = handlerParameters.sourceLocation
    val path = handlerParameters.path
    val error =
        when (exception) {
          is GraphQlExecutionException ->
              GraphQLError.newError().message(exception.message).path(path).build()
          else -> ExceptionWhileDataFetching(path, exception, sourceLocation)
        }
    val result = DataFetcherExceptionHandlerResult.newResult().error(error).build()
    return CompletableFuture.completedFuture(result)
  }
}
