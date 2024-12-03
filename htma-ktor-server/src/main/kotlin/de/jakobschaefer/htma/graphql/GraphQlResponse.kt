package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.ApolloResponse

data class GraphQlResponse(
  val success: Boolean,
  val data: Any?,
  val errors: List<GraphQlError>,
  val performed: Boolean
)

data class GraphQlError(
  val message: String
)

fun ApolloResponse<*>.toGraphQlResponse(): GraphQlResponse {
  return GraphQlResponse(
    success = !hasErrors(),
    data = data,
    errors = errors?.map { GraphQlError(it.message) } ?: emptyList(),
    performed = true
  )
}
