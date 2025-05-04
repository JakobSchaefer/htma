package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestGraphQlDocument(
  val queries: List<AppManifestGraphQlOperation>,
  val mutations: List<AppManifestGraphQlOperation>,
)

