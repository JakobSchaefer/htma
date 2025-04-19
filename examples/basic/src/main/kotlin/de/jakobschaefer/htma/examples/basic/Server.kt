package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.examples.basic.graphql.*
import de.jakobschaefer.htma.routing.web
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object Heroes : Table("heroes") {
  val id = integer("id").autoIncrement()
  val name = varchar("name", 50)
  val level = integer("level")
}

fun Application.module() {
  Database.connect("jdbc:h2:./dev/data", driver = "org.h2.Driver")

  transaction {
    SchemaUtils.create(Heroes)
  }

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
