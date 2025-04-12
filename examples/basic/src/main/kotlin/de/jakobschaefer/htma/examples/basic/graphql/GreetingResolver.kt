package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment

class GreetingResolver : GraphQlDataResolver<String> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): String {
    val name = env.arguments["name"] as String? ?: getName()
    return "Hello, $name!"
  }
}
