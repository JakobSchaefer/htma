package de.jakobschaefer.htma.routing

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HtmaClientNavigationContext(
  @SerialName("_target")
  val target: String,
  @SerialName("_service")
  val service: String?,
  @SerialName("_operation")
  val operation: String?
) {
  companion object {
    fun fromParameters(parameters: Parameters): HtmaClientNavigationContext {
      return HtmaClientNavigationContext(
        target = parameters["_target"]!!,
        service = parameters["_service"],
        operation = parameters["_operation"]
      )
    }
  }
}
