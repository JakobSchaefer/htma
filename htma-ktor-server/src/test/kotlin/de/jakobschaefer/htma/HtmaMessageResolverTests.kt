package de.jakobschaefer.htma

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.javamoney.moneta.Money
import java.util.*

class HtmaMessageResolverTests : FunSpec({

  val testData = mapOf(
    "name" to "Max",
    "now" to LocalDateTime(year = 2024, monthNumber = 1, dayOfMonth = 1, hour = 16, minute = 30, second = 0, nanosecond = 0),
    "price" to Money.of(11.99, "EUR")
  )

  test("simple message resolution") {
    withTestServer {
      install(Htma) {
        fallbackLocale = Locale.ENGLISH
        resourceBase = "/message-resolver-tests"
      }

      routing {
        get("/") {
          call.respondTemplate("index", testData)
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("greeting")!!.text() shouldBe "Hello!"
    }
  }

  test("message with parameters") {
    withTestServer {
      install(Htma) {
        fallbackLocale = Locale.ENGLISH
        resourceBase = "/message-resolver-tests"
      }

      routing {
        get("/") {
          call.respondTemplate("index", testData)
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("greetingTo")!!.text() shouldBe "Hello Max!"
    }
  }

  test("datetime formatting with kotlinx-datetime") {
    withTestServer {
      install(Htma) {
        fallbackLocale = Locale.ENGLISH
        resourceBase = "/message-resolver-tests"
      }

      routing {
        get("/") {
          call.respondTemplate("index", testData)
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("greetingToWithDateTime")!!.text() shouldBe "Hello Max! It's 4:30 PM on Monday, January 1, 2024."
    }
  }

  test("support for javas MonetaryAmount") {
    withTestServer {
      install(Htma) {
        fallbackLocale = Locale.ENGLISH
        resourceBase = "/message-resolver-tests"
      }

      routing {
        get("/") {
          call.respondTemplate("index", testData)
        }
      }

      val html = client.get("/").bodyAsHtml()
      html.getElementById("price")!!.text() shouldBe "Price: €11.99"
    }
  }
})
