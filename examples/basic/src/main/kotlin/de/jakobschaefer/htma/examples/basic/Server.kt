package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.examples.basic.graphql.*
import de.jakobschaefer.htma.routing.web
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authentication
import io.ktor.server.auth.oauth
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Heroes : Table("heroes") {
  val id = integer("id").autoIncrement()
  val name = varchar("name", 50)
  val level = integer("level")
}

object UserSessions : Table("user_sessions") {
  val id = char("id", 36)
  val state = varchar("state", 256)

  override val primaryKey: PrimaryKey = PrimaryKey(id)
}

class SessionStorageDatabase : SessionStorage {
  override suspend fun write(id: String, value: String) {
    newSuspendedTransaction {
      UserSessions.insert {
        it[UserSessions.id] = id
        it[UserSessions.state] = value
      }
    }
  }

  override suspend fun invalidate(id: String) {
    newSuspendedTransaction {
      UserSessions.deleteWhere { UserSessions.id eq id }
    }
  }

  override suspend fun read(id: String): String {
    return newSuspendedTransaction {
      UserSessions.select(UserSessions.state).where { UserSessions.id eq id }
        .singleOrNull()?.get(UserSessions.state) ?: ""
    }
  }
}

@OptIn(ExperimentalStdlibApi::class)
fun Application.module() {
  Database.connect("jdbc:h2:./dev/data", driver = "org.h2.Driver")

  transaction {
    SchemaUtils.create(Heroes, UserSessions)
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
class UserSession(accessToken: String)
