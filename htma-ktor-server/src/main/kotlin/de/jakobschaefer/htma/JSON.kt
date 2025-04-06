package de.jakobschaefer.htma

import kotlinx.serialization.json.Json

internal val JSON = Json {
  prettyPrint = true
  ignoreUnknownKeys = true
}
