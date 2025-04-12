package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestGraphQlOperation(
  val operationName: String,
  val operation: String,
)
