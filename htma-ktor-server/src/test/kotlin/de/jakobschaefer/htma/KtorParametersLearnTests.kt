package de.jakobschaefer.htma

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.*

class KtorParametersLearnTests : FunSpec({
  test("parameters are extendable") {
    val params = Parameters.build {
      set("foo", "bar")
    }

    val extendedParams = params.plus(Parameters.build {
      set("x", "y")
    })

    extendedParams["foo"] shouldBe "bar"
    extendedParams["x"] shouldBe "y"
  }
})
