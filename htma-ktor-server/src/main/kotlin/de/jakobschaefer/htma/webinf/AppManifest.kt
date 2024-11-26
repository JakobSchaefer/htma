package de.jakobschaefer.htma.webinf

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
    private val json = Json {
      ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromResources(resourceBase: String): AppManifest {
      val manifest = loadResource("${resourceBase}/manifest.json")
      val appManifest = json.decodeFromStream<AppManifest>(manifest)
      return appManifest
    }
  }
}

private fun loadResource(path: String): InputStream {
  return object {}.javaClass.getResourceAsStream(path)!!
}
