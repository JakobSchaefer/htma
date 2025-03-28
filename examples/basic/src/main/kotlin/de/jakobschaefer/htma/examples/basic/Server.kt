package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.routing.htma
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation


fun Application.module() {
  install(Htma)

  install(ContentNegotiation) {
    json()
  }

  val keycloak = Keycloak.getInstance(
    "http://localhost:8081",
    "master",
    "admin",
    "admin",
    "admin-cli"
  )

  routing {
    htma {
    }

    post("/api/create-customer-admin") {
      val payload = call.receive<CustomerAdminRegistrationRequest>()
      if (payload.customerNumber != "D-12345") {
        call.respond(HttpStatusCode.BadRequest)
      } else {
        val oneHubRealm = keycloak.realm("one-hub")
        val keycloakUser = UserRepresentation().apply {
          username = payload.email
          firstName = payload.firstName
          lastName = payload.lastName
          email = payload.email
          isEnabled = true
        }
        val keycloakResponse = oneHubRealm.users().create(keycloakUser)
        println("createUser@keycloak(email = ${payload.email}, status = ${keycloakResponse.status})")
        val user = oneHubRealm.users().search(payload.email).first()
        oneHubRealm.users()
          .get(user.id)
          .sendVerifyEmail()
//          .executeActionsEmail(
//            "portal",
//            "http://localhost:5173/cdn/onboarding-service-frontend/0.0.1/index.html",
//            listOf("VERIFY_EMAIL", "UPDATE_PASSWORD")
//          )
        call.respond(CustomerAdminRegistrationReply(jobId = "1"))
      }
    }
  }
}

@Serializable
data class CustomerAdminRegistrationRequest(
  val email: String,
  val firstName: String,
  val lastName: String,
  val licenseKey: String,
  val customerNumber: String,
  val organisationDomain: String
)

@Serializable
data class CustomerAdminRegistrationReply(
  val jobId: String
)

data class IndexData(val items: List<Int>)
