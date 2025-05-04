package de.jakobschaefer.htma.graphql.inputvalidation

import io.konform.validation.Validation

abstract class InputTypeHelper<T> {
  abstract val validation: Validation<T>
  abstract fun fromMap(data: Map<String, Any>): T

  fun fromArguments(data: Map<String, Any>): T {
    val input = fromMap(data)
    val validationResult = validation(input)
    if (!validationResult.isValid) {
      throw InvalidInputException(validationResult)
    } else {
      return input
    }
  }
}
