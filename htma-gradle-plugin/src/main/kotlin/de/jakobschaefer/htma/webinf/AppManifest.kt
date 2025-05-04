package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>,
  val components: List<AppComponent>,
  val graphQlDocuments: Map<String, AppManifestGraphQlDocument>
) {

  fun toJson() = JsonConverter.encodeToString(this)

}
