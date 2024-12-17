package de.jakobschaefer.graphql

import com.google.gson.GsonBuilder
import graphql.schema.idl.RuntimeWiring

class GraphQlSchemaWiring {
  val runtimeWiring = RuntimeWiring.newRuntimeWiring()

  fun build(): RuntimeWiring {
    return runtimeWiring.build()
  }

  companion object {
    private val gson = GsonBuilder().create()

    fun <T> parseArgument(input: Any?, javaClass: Class<T>): T {
      val json = gson.toJson(input)
      return gson.fromJson(json, javaClass)
    }
  }
}

