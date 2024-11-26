package de.jakobschaefer.htma.serde

import kotlinx.serialization.json.Json

val JsonConverter = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
}
