package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.routing.web
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import java.util.*

fun Application.module() {
  install(Htma)

  install(Sessions) {
    cookie<MySession>("session") {
      cookie.extensions["SameSite"] = "lax"
      cookie.secure = true
    }
  }

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

  val jobStates = mutableMapOf<String, AdminAccountCreationStatus>()

  routing {
    web {
      loader("common", "__root") {
        mapOf("some" to "variable")
      }
      loader("currentYear", "__root.variables") {
        Calendar.getInstance().get(Calendar.YEAR)
      }
    }

    post("/api/get-admin-account-creation-status") {
      val payload = call.receive<AdminAccountStatusRequest>()
      val jobState = when (val currentJobState = jobStates[payload.jobId] ?: AdminAccountCreationStatus.UNKNOWN) {
        AdminAccountCreationStatus.IN_CREATION -> {
          val isEmailVerified = keycloak.realm("one-hub")
            .users()
            .get(payload.jobId)
            .toRepresentation()
            .isEmailVerified
          if (isEmailVerified) {
            jobStates[payload.jobId] = AdminAccountCreationStatus.PENDING_ACTIVATION
            AdminAccountCreationStatus.PENDING_ACTIVATION
          } else {
            AdminAccountCreationStatus.IN_CREATION
          }
        }

        else -> currentJobState
      }
      call.respond(AdminAccountStatusReply(jobId = payload.jobId, jobStatus = jobState.ordinal))
    }

    post("/api/activate/{jobId}") {
      val jobId = call.parameters["jobId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
      jobStates[jobId] = AdminAccountCreationStatus.ACTIVE
      call.respond(HttpStatusCode.OK)
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
          credentials = listOf(CredentialRepresentation().apply {
            isTemporary = false
            type = CredentialRepresentation.PASSWORD
            value = "test"
          })
        }
        val keycloakResponse = oneHubRealm.users().create(keycloakUser)
        println("createUser@keycloak(email = ${payload.email}, status = ${keycloakResponse.status})")
        val user = oneHubRealm.users().search(payload.email).first()
        oneHubRealm.users()
          .get(user.id)
          .sendVerifyEmail()
        val jobId = user.id
        jobStates[jobId] = AdminAccountCreationStatus.IN_CREATION
        call.respond(CustomerAdminRegistrationReply(jobId = jobId))
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

@Serializable
data class AdminAccountStatusRequest(
  val jobId: String
)

@Serializable
enum class AdminAccountCreationStatus {
  UNKNOWN,
  FAILURE,
  IN_CREATION,
  PENDING_ACTIVATION,
  ACTIVE,
}

@Serializable
data class AdminAccountStatusReply(
  val jobId: String,
  val jobStatus: Int,
  val message: String? = null
)

@Serializable
data class MySession(val count: Int = 0)
