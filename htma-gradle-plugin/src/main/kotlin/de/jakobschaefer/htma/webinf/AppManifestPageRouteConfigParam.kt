package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPageRouteConfigParam(
  val name: String,
  val type: String = "String",
  val required: Boolean = false,
  val default: List<String>?
)
