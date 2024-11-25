package de.jakobschaefer.htma;

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.ktor.server.testing.*

class HtmaSmokeTests : FunSpec({
  test("plugin can be started without errors") {
    val logs = captureLogs(Logs.htma)
    testApplication {
      install(Htma)
    }
    logs.events.map { it.message.formattedMessage } shouldContain "Htma plugin started!"
  }
})
