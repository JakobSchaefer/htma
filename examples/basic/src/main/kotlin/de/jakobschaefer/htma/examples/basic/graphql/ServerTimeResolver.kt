package de.jakobschaefer.htma.examples.basic.graphql

import de.jakobschaefer.htma.graphql.GraphQlDataResolver
import de.jakobschaefer.htma.graphql.GraphQlDataResolverEnvironment
import kotlinx.datetime.Clock

class ServerTimeResolver : GraphQlDataResolver<Long> {
  override suspend fun resolve(env: GraphQlDataResolverEnvironment): Long {
    val millis = Clock.System.now().toEpochMilliseconds()
    return millis
  }
}
