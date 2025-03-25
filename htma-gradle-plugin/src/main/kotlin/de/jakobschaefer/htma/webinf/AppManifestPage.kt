package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPage(
  val remotePath: String,
  val outlets: Map<String, String>,
)
