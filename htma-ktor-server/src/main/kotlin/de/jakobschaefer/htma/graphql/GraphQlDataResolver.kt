package de.jakobschaefer.htma.graphql

interface GraphQlDataResolver<T> {
  suspend fun resolve(env: GraphQlDataResolverEnvironment): T
}
