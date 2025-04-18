package de.jakobschaefer.htma;

import de.jakobschaefer.htma.routing.web
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.http.*

class HtmaSmokeTests : FunSpec({
  test("plugin can be started without errors") {
    val logs = captureLogs(Logs.htma)
    withTestServer {
      install(Htma)
    }
    logs.events.map { it.message.formattedMessage } shouldContain "Htma plugin has been configured!"
  }

  test("__root layout is working properly") {
    withTestServer {
      install(Htma)

      routing {
        web {
        }
      }
      val response = client.get("/")
      response.status shouldBe HttpStatusCode.OK

      val title = response.bodyAsHtml().select("h1")
      title.text() shouldBe "Index"
    }
  }
})
