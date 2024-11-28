package de.jakobschaefer.htma

import de.jakobschaefer.htma.routing.htma
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.routing.*

class TemplateResolverTests : FunSpec({
  test("template can be resolved and returned manually") {
    withTestServer {
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
    withTestServer {
      install(Htma) {
        resourceBase = "/template-resolver-tests"
      }
      routing {
        htma {
        }
      }
      val response = client.get("/route")
      response.bodyAsText() shouldContain """<title>Route</title>"""
    }
  }

  test("fragments of internal templates can be used") {
    withTestServer {
      install(Htma) {
        resourceBase = "/template-resolver-tests"
      }

      routing {
        htma { }
      }

      val response = client.get("/").bodyAsText()
      response shouldContain """<meta charset="UTF-8">"""
      response shouldContain """<title>Index</title>"""
    }
  }
})
