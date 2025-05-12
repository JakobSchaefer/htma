package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>,
  val webComponents: List<AppManifestWebComponent>,
  val graphQlDocuments: Map<String, AppManifestGraphQlDocument>
) {
  fun toJson() = JsonConverter.encodeToString(this)
}
