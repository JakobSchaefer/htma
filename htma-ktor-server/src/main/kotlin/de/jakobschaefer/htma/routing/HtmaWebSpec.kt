package de.jakobschaefer.htma.routing

import io.ktor.utils.io.*


class HtmaWebSpec(
  val dataLoaders: List<HtmaDataLoader>
)

class HtmaWebSpecBuilder {
  private val dataLoaders = mutableListOf<HtmaDataLoader>()

  @KtorDsl
  fun loader(name: String, canonicalPath: String, load: HtmaDataLoadFunction) {
    dataLoaders.add(HtmaDataLoader(name, canonicalPath, load))
  }

  fun build(): HtmaWebSpec {
    return HtmaWebSpec(dataLoaders)
  }
}
