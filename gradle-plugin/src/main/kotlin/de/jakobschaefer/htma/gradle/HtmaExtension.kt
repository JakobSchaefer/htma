package de.jakobschaefer.htma.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class HtmaExtension @Inject constructor(private val objects: ObjectFactory) {
  val resourceBase = objects.property<String>().convention("/WEB-INF")
  val webDir = objects.property<String>().convention("web")
}
