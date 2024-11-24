package de.jakobschaefer.htma.webinf.app

import kotlinx.serialization.Serializable

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>
)
