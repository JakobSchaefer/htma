package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestWebComponent(
  val name: String,
  val templateName: String
)
