package de.jakobschaefer.htma.routing

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HtmaClientNavigationContext(
  @SerialName("_t")
  val target: String?,
  @SerialName("_s")
  val service: String?,
  @SerialName("_o")
  val operation: String?
) {
  companion object {
    fun fromParameters(parameters: Parameters): HtmaClientNavigationContext {
      return HtmaClientNavigationContext(
        target = parameters["_t"],
        service = parameters["_s"],
        operation = parameters["_o"]
      )
    }
  }
}
