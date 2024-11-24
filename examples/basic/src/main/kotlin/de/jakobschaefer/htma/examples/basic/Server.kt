package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.module() {
  install(Htma)

  routing {
    get("/") {
      call.respondText("Hello World!")
    }
  }
}
