package de.jakobschaefer.htma.graphql

import de.jakobschaefer.htma.routing.HtmaParams
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GraphQlParamsToVariablesConverterTests : FunSpec({
  test("simple lookup succeeds") {
    val params: HtmaParams = mapOf(
      "${'$'}name" to listOf("Test"),
    )
    val variables = GraphQlParamsToVariablesConverter.convert(params)
    variables shouldBe mapOf(
      "name" to "Test"
    )
  }

  test("non graphql params will be ignored") {
    val params: HtmaParams = mapOf(
      "name" to listOf("Test"),
    )
    val variables = GraphQlParamsToVariablesConverter.convert(params)
    variables shouldBe emptyMap()
  }

  test("variables can be nested") {
    val params: HtmaParams = mapOf(
      "${'$'}hero.name" to listOf("Superman"),
      "${'$'}hero.level" to listOf("9001"),
    )
    val variables = GraphQlParamsToVariablesConverter.convert(params)
    variables shouldBe mapOf(
      "hero" to mapOf(
        "name" to "Superman",
        "level" to "9001",
      )
    )
  }

  test("variables can also by lists") {
    val params: HtmaParams = mapOf(
      "${'$'}hero.name" to listOf("Superman"),
      "${'$'}hero.achievements[]" to listOf("Survival", "Monsters", "Strongest", "Sun"),
    )
    val variables = GraphQlParamsToVariablesConverter.convert(params)
    variables shouldBe mapOf(
      "hero" to mapOf(
        "name" to "Superman",
        "achievements" to listOf("Survival", "Monsters", "Strongest", "Sun"),
      )
    )
  }

  test("variables can be deeply nested") {
    val params: HtmaParams = mapOf(
      "${'$'}person.address.street.houseNumber" to listOf("1"),
      "${'$'}hero.guild[0].location.name" to listOf("Marvel"),
    )
    val variables = GraphQlParamsToVariablesConverter.convert(params)
    variables shouldBe mapOf(
      "person" to mapOf(
        "address" to mapOf(
          "street" to mapOf(
            "houseNumber" to "1",
          )
        ),
      ),
      "hero" to mapOf(
        "guild" to listOf(
          mapOf("location" to mapOf("name" to "Marvel")),
        ),
      ),
    )
  }
})
