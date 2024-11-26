package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>
) {

  fun toJson() = json.encodeToString(this)

  companion object {
    private val json = Json {
      ignoreUnknownKeys = true
      prettyPrint = true // Developers should be able to read the produced manifest
    }
  }
}
