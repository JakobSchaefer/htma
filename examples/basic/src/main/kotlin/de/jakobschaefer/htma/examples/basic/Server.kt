package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
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

  install(Htma)

  routing {
    web {

    }

    get("/heroes/{heroId}") {

    }
//    web({ RequestContext("Hansi") }) {
//      loader("common", "__root") {
//        mapOf("some" to "variable")
//      }
//      loader("currentYear", "__root.variables") {
//        Calendar.getInstance().get(Calendar.YEAR)
//      }
//      graphql("internal") {
//        local("graphql/schema.graphqls") {
//          isPublic = true
//          publishPath = "/graphql"
//          type("Query") {
//            field("name", NameResolver())
//            field("greeting", GreetingResolver())
//          }
//        }
//      }
//    }
  }
}
