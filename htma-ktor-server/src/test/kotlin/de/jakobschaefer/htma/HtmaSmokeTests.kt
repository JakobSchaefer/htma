package de.jakobschaefer.htma;

import de.jakobschaefer.htma.routing.htma
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class HtmaSmokeTests : FunSpec({
  test("plugin can be started without errors") {
    val logs = captureLogs(Logs.htma)
    withTestServer {
      install(Htma)
    }
    logs.events.map { it.message.formattedMessage } shouldContain "Htma plugin started!"
  }

  test("__root layout is working properly") {
    withTestServer {
      install(Htma)

      routing {
        htma {  }
      }
      val response = client.get("/")
      response.status shouldBe HttpStatusCode.OK

      val title = response.bodyAsHtml().select("h1")
      title.text() shouldBe "Index"
    }
  }
})
