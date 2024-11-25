package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.routing.web
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {
  install(Htma)

  routing {
    web {
    }
  }
}
