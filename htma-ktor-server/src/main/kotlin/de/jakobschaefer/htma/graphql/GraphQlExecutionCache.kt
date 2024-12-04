package de.jakobschaefer.htma.graphql

import java.util.concurrent.ConcurrentHashMap

data class GraphQlExecutionCache(
  val entries: ConcurrentHashMap<GraphQlOperationRef, GraphQlResponse>,
)
