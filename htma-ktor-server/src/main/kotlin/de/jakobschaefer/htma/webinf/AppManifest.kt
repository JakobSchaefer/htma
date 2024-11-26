package de.jakobschaefer.htma.webinf

import de.jakobschaefer.htma.serde.JsonConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>
) {

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromResources(resourceBase: String): AppManifest {
      val manifest = loadResource("${resourceBase}/manifest.json")
      val appManifest = JsonConverter.decodeFromStream<AppManifest>(manifest)
      return appManifest
    }
  }
}

