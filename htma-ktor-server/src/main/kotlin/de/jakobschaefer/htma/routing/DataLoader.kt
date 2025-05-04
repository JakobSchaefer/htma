package de.jakobschaefer.htma.routing

import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext

data class DataLoader(
  val canonicalPath: String
)

class DataLoaderBuilder(
  val canonicalPath: String,
) : HtmaRoutingDslBuilder<DataLoader> {

  fun validate() {}

  override fun build(): DataLoader {
    return DataLoader(canonicalPath)
  }
}
