package de.jakobschaefer.htma.webinf.vite

import de.jakobschaefer.htma.JSON
import de.jakobschaefer.htma.loadAppResource
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@Serializable
data class ViteManifest(
  val assets: Map<String, ViteChunk>,
  val mainJsModules: List<String>,
  val mainCssModules: List<String>
) {

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromResources(resourceBase: String): ViteManifest {
      val manifest = loadAppResource("$resourceBase/.vite/manifest.json")
      val assets = JSON.decodeFromStream<Map<String, ViteChunk>>(manifest)
      val mainChunk = assets.values.find { it.isEntry }!!
      return ViteManifest(
        assets = assets,
        mainJsModules = listOf("/" + mainChunk.file),
        mainCssModules = mainChunk.css.map { "/${it}" }
      )
    }

    fun development(): ViteManifest {
      val viteServerPort = 5174
      return ViteManifest(
        assets = emptyMap(),
        mainJsModules = listOf("http://localhost:$viteServerPort/@vite/client", "http://localhost:$viteServerPort/web/__root.js"),
        mainCssModules = listOf("http://localhost:$viteServerPort/web/__root.css")
      )
    }

    private fun getVitePort(viteConfig: String): Int {
      val portString = viteConfig.replace(Regex("\\s"), "")
        .substringAfter("server:{", "")
        .substringAfter("port:", "")
        .substringBefore(",", "")
      if (portString.isNotEmpty()) {
        return portString.toInt()
      } else {
        return 5173
      }
    }
  }
}
