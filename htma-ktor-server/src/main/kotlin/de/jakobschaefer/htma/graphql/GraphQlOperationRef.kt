package de.jakobschaefer.htma.graphql

/**
 * Representation of a graphql operation used in a template via the th:query or th:mutation attribute
 */
data class GraphQlOperationRef(
  val serviceName: String,
  val operationName: String,
  val variables: Map<String, Any>
)
