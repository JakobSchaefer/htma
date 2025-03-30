package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPage(
  val filePath: String,
  val remotePath: String,
  val canonicalPath: String,
  val outlets: Map<String, String>,
)
