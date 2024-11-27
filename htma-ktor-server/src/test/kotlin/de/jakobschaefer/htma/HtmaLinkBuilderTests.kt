package de.jakobschaefer.htma

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.server.routing.*

class HtmaLinkBuilderTests : FunSpec({
  test("absolute links will be kept untouched") {
    withTestServer {
      install(Htma) {
        resourceBase = "/link-builder-tests"
      }
      routing {
        get("/") {
          call.respondTemplate("index", mapOf("link" to "/"))
        }
        get("/abcd/123456") {
          call.respondTemplate("index", mapOf("link" to "/abcd/123456"))
        }
      }

      val rootHtml = client.get("/").bodyAsHtml()
      rootHtml.getElementById("link")!!.attribute("href")!!.value shouldBe "/"

      val abcdHtml = client.get("/abcd/123456").bodyAsHtml()
      abcdHtml.getElementById("link")!!.attribute("href")!!.value shouldBe "/abcd/123456"
    }
  }

  test("relative links will lookup resource in the vite assets") {
    withTestServer {
      install(Htma) {
        resourceBase = "/link-builder-tests"
      }
      routing {
        get("/") {
          call.respondTemplate("index", mapOf("link" to "main.js"))
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("link")!!.attribute("href")!!.value shouldBe "/assets/main.js"
    }
  }

  test("relative links will be kept untouched if asset does not exist") {
    withTestServer {
      install(Htma) {
        resourceBase = "/link-builder-tests"
      }
      routing {
        get("/") {
          call.respondTemplate("index", mapOf("link" to "unknown/relative/link.js"))
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("link")!!.attribute("href")!!.value shouldBe "/unknown/relative/link.js"
    }
  }

  test("urls will not be changed") {
    withTestServer {
      install(Htma) {
        resourceBase = "/link-builder-tests"
      }
      routing {
        get("/") {
          call.respondTemplate("index", mapOf("link" to "http://localhost:5173/@vite/client"))
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("link")!!.attribute("href")!!.value shouldBe "http://localhost:5173/@vite/client"
    }
  }

  test("thymeleaf's server relative path will be treated as an absolute path") {
    withTestServer {
      install(Htma) {
        resourceBase = "/link-builder-tests"
      }
      routing {
        get("/") {
          call.respondTemplate("index", mapOf("link" to "~/server-relative-path"))
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("link")!!.attribute("href")!!.value shouldBe "/server-relative-path"
    }
  }
})
