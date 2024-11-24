package de.jakobschaefer.htma

import io.ktor.server.application.*

val Htma = createApplicationPlugin("Htma") {
  Logs.htma.info("Htma plugin started!")
}
