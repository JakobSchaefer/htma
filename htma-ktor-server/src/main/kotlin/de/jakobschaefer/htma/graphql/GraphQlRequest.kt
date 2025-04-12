package de.jakobschaefer.htma.graphql

class GraphQlRequest(
  val query: String,
  val operationName: String?,
  val variables: Map<String, Any?>
)
