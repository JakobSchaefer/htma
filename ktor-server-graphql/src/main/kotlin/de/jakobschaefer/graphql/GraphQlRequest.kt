package de.jakobschaefer.graphql

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GraphQlRequest(
    val query: String,
    val operationName: String? = null,
    val variables: JsonObject? = null,
)
