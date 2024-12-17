package de.jakobschaefer.graphql

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

open class GraphQlExtension @Inject constructor(private val objects: ObjectFactory) {
  val packageName = objects.property<String>().convention("de.jakobschaefer.graphql")
  val outputFile = objects.property<String>().convention("SchemaModel.kt")
  val serviceName = objects.property<String>().convention("graphql")
  val schemaFile = objects.fileProperty().convention { File("src/main/resources/graphql/schema.graphqls") }
}

