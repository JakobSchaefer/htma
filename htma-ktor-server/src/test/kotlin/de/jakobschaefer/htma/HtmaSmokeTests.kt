package de.jakobschaefer.htma;

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain

class HtmaSmokeTests : FunSpec({
  test("plugin can be started without errors") {
    val logs = captureLogs(Logs.htma)
    withTestServer {
      install(Htma)
    }
    logs.events.map { it.message.formattedMessage } shouldContain "Htma plugin started!"
  }
})
