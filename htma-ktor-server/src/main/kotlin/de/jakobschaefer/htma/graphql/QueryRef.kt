package de.jakobschaefer.htma.graphql

/**
 * Representation of a graphql query used in a template via the th:query attribute
 */
data class QueryRef(
  val serviceName: String,
  val queryName: String,
  val queryParameters: List<Any>
)
