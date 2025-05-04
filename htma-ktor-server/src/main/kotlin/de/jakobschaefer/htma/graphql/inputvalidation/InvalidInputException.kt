package de.jakobschaefer.htma.graphql.inputvalidation

import io.konform.validation.ValidationResult

class InvalidInputException(val validationResult: ValidationResult<*>) : Exception("Invalid input")
