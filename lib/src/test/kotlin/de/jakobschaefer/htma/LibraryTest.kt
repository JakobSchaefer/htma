package de.jakobschaefer.htma;

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LibraryTest : FunSpec({
  test("someLibraryMethodReturnsTrue") {
    val classUnderTest = Library()
    classUnderTest.someLibraryMethod() shouldBe true
  }
})
