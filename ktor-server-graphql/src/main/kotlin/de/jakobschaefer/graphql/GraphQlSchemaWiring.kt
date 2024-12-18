package de.jakobschaefer.graphql

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import graphql.schema.idl.RuntimeWiring

class GraphQlSchemaWiring<T>(
  val runtimeWiring: RuntimeWiring.Builder = RuntimeWiring.newRuntimeWiring()
) {
  fun build(): RuntimeWiring {
    return runtimeWiring.build()
  }

  companion object {
    val gson = GsonBuilder().create()

    inline fun <reified T> parseArgument(input: Any?): T {
      val type = object : TypeToken<T>() {}.type
      val json = gson.toJson(input, type)
      return gson.fromJson(json, type)
    }
  }
}

