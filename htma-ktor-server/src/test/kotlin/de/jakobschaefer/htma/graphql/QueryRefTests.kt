package de.jakobschaefer.htma.graphql

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QueryRefTests : FunSpec({
  test("query ref variables are part of the identity") {
    // A QueryRef is used as a caching key. Comparison must be by-value
    val a = GraphQlOperationRef(
      serviceName = "template",
      operationName = "Query",
      variables = mapOf("a" to "b")
    )
    val b = a.copy()
    (a == b) shouldBe true
  }

  test("changes in query variables will lead to a different hashCode") {
    val a = GraphQlOperationRef(
      serviceName = "template",
      operationName = "Query",
      variables = mapOf("a" to "b")
    )
    val b = a.copy(
      variables = mapOf("a" to "x")
    )
    (a == b) shouldBe false
  }
})
