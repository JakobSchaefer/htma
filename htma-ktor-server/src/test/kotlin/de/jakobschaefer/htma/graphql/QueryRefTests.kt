package de.jakobschaefer.htma.graphql

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class QueryRefTests : FunSpec({
  test("query ref variables are part of the identity") {
    // A QueryRef is used as a caching key. Comparison must be by-value
    val a = QueryRef(
      serviceName = "template",
      queryName = "Query",
      queryParameters = listOf("a", "b", "c")
    )
    val b = a.copy()
    (a == b) shouldBe true
  }

  test("changes in query variables will lead to a different hashCode") {
    val a = QueryRef(
      serviceName = "template",
      queryName = "Query",
      queryParameters = listOf("a", "b", "c")
    )
    val b = a.copy(
      queryParameters = listOf("a", "b", "X")
    )
    (a == b) shouldBe false
  }
})
