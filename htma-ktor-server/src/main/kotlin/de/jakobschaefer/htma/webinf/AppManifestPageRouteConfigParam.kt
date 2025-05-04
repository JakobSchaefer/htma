package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPageRouteConfigParam(
  val name: String,
  val type: String,
  val required: Boolean,
  val default: List<String>
)
