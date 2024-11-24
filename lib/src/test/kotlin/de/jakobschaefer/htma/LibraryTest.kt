package de.jakobschaefer.htma;

import io.kotest.core.spec.style.FunSpec
import io.ktor.server.testing.*

class LibraryTest : FunSpec({
  test("Htma plugin can be installed") {
    testApplication {
      install(Htma)
    }
  }
})
