package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment

var THE_NAME: String? = null
fun getName(): String {
  return THE_NAME ?: "World"
}

class NameResolver : GraphQlDataResolver<String> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): String {
    return getName()
  }
}

