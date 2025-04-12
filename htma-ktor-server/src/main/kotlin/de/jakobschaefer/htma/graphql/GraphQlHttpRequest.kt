package de.jakobschaefer.htma.graphql

import kotlinx.serialization.Serializable

@Serializable
data class GraphQlHttpRequest(
  val query: String
)
