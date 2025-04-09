package de.jakobschaefer.htma.rendering.jexl

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.rendering.HtmaState
import de.jakobschaefer.htma.routing.HtmaParams
import io.ktor.server.routing.*
import org.apache.commons.jexl3.MapContext
import java.util.Locale

internal class HtmaContext(
  val call: RoutingCall,
  val locale: Locale,
  val htmaState: HtmaState,
  val params: HtmaParams,
  val configuration: HtmaConfiguration
) : MapContext() {

  init {
    set("locale", locale)
    set("params", params)
    set("htma", htmaState)
  }
}
