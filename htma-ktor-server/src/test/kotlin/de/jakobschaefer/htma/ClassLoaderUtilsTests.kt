package de.jakobschaefer.htma

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldStartWith

class ClassLoaderUtilsTests : FunSpec({
  test("resource loading") {
    val log4j2Xml = loadAppResource("/log4j2.xml").readAllBytes().decodeToString()
    log4j2Xml shouldStartWith "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
  }

  test("resource loading without leading slash") {
    val log4j2Xml = loadAppResource("log4j2.xml").readAllBytes().decodeToString()
    log4j2Xml shouldStartWith "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
  }
})
