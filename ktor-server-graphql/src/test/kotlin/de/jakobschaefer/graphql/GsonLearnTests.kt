package de.jakobschaefer.graphql

import com.google.gson.GsonBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GsonLearnTests : FunSpec({
  test("gson serializes json string primitives") {
    val gson = GsonBuilder().create()

    val jsonString = gson.toJson("test")
    jsonString shouldBe "\"test\""
  }
  test("gson serializes json boolean primitives") {
    val gson = GsonBuilder().create()

    val jsonString = gson.toJson(true)
    jsonString shouldBe "true"
  }
  test("gson serializes json null primitives") {
    val gson = GsonBuilder().create()

    val jsonString = gson.toJson(null)
    jsonString shouldBe "null"
  }

  test("gson deserializes null to kotlin-null") {
    val gson = GsonBuilder().create()

    val kotlinNull = gson.fromJson("null", String::class.java)
    kotlinNull shouldBe null
  }
})
