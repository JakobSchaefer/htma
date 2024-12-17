package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.Optional
import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApolloOptionalTypeAdapter<T> : JsonSerializer<Optional<T>>, JsonDeserializer<Optional<T>> {
  override fun serialize(src: Optional<T>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
    return when (src) {
      is Optional.Present -> context.serialize(src.value)
      is Optional.Absent -> JsonNull.INSTANCE
    }
  }

  override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Optional<T> {
    return if (json.isJsonNull) {
      Optional.absent()
    } else {
      val value = context.deserialize<T>(json, (typeOfT as ParameterizedType).actualTypeArguments[0])
      Optional.presentIfNotNull(value)
    }
  }
}
