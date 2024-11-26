package de.jakobschaefer.htma

import com.typesafe.config.ConfigFactory
import de.jakobschaefer.htma.routing.web
import io.kotest.core.config.configuration
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*

class TemplateResolverTests : FunSpec({
  test("template can be resolved and returned manually") {
    testApplication {
      serverConfig {
        developmentMode = false
      }
      install(Htma) {
        resourceBase = "/template-resolver-tests"
      }
      routing {
        get("/test") {
          call.respondTemplate("route", emptyMap())
        }
      }
      val response = client.get("/test")
      response.bodyAsText() shouldContain """<title>Route</title>"""
    }
  }

  test("templates are resolved according to app manifest") {
    testApplication {
      serverConfig {
        developmentMode = false
      }
      install(Htma) {
        resourceBase = "/template-resolver-tests"
      }
      routing {
        web {
        }
      }
      val response = client.get("/route")
      response.bodyAsText() shouldContain """<title>Route</title>"""
    }
  }

  test("fragments of internal templates can be used") {
    testApplication {
      serverConfig {
        developmentMode = false
      }
      install(Htma) {
        resourceBase = "/template-resolver-tests"
      }

      routing {
        web { }
      }

      val response = client.get("/").bodyAsText()
      response shouldContain """<meta charset="UTF-8">"""
      response shouldContain """<title>Index</title>"""
    }
  }
})
