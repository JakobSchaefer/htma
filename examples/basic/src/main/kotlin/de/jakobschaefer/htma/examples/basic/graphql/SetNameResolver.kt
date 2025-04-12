package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment

class SetNameResolver : GraphQlDataResolver<String> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): String {
    THE_NAME = env.arguments["name"] as String?
    return getName()
  }
}
