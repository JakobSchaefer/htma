package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.examples.basic.graphql.*
import de.jakobschaefer.htma.routing.web
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.module() {
  install(Sessions) {
    cookie<Session>("session") {
      cookie.extensions["SameSite"] = "lax"
      cookie.secure = true
    }
  }

  install(Htma) {
    graphql {
      type("Query") {
        resolve("name", NameResolver())
        resolve("greeting", GreetingResolver())
        resolve("serverTime", ServerTimeResolver())
        resolve("meals", MealsResolver())
      }
      type("Mutation") {
        resolve("setName", SetNameResolver())
      }
    }
  }

  routing {
    web {
    }
  }
}
