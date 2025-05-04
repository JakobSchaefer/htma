package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPageRouteConfig(
  val params: List<AppManifestPageRouteConfigParam>
)

