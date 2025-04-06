package de.jakobschaefer.htma.webinf

import de.jakobschaefer.htma.JSON
import de.jakobschaefer.htma.loadAppResource
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>,
  val components: List<AppComponent>,
) {

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromResources(resourceBase: String): AppManifest {
      val manifest = loadAppResource("${resourceBase}/manifest.json")
      val appManifest = JSON.decodeFromStream<AppManifest>(manifest)
      return appManifest
    }
  }
}

