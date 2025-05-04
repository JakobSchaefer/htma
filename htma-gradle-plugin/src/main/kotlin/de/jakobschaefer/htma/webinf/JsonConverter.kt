package de.jakobschaefer.htma.webinf

import kotlinx.serialization.json.Json

val JsonConverter = Json {
  ignoreUnknownKeys = true
  prettyPrint = true // Developers should be able to read the produced manifest
}
