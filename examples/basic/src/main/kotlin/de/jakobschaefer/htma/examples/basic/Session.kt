package de.jakobschaefer.htma.examples.basic

import kotlinx.serialization.Serializable

@Serializable
data class Session(
  val id: String,
  val preferredLocale: String
)
