package de.jakobschaefer.htma.webinf.vite

import de.jakobschaefer.htma.serde.JsonConverter
import de.jakobschaefer.htma.webinf.loadResource
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream

data class ViteManifest(
  val assets: Map<String, ViteChunk>,
  val mainJsModules: List<String>,
  val mainCssModules: List<String>
) {

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromResources(resourceBase: String): ViteManifest {
      val manifest = loadResource("$resourceBase/.vite/manifest.json")
      val assets = JsonConverter.decodeFromStream<Map<String, ViteChunk>>(manifest)
      val mainChunk = assets.values.find { it.isEntry }!!
      return ViteManifest(
        assets = assets,
        mainJsModules = listOf(mainChunk.file),
        mainCssModules = mainChunk.css
      )
    }

    fun development(): ViteManifest {
      return ViteManifest(
        assets = emptyMap(),
        mainJsModules = listOf("http://localhost:5173/@vite/client", "http://localhost:5173/web/main.js"),
        mainCssModules = emptyList()
      )
    }
  }
}
