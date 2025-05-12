package de.jakobschaefer.htma.rendering.jexl

import de.jakobschaefer.htma.messages.HtmaMessageFormatter
import de.jakobschaefer.htma.rendering.state.HtmaState
import de.jakobschaefer.htma.rendering.htma
import de.jakobschaefer.htma.rendering.thymeleaf.messageFormatter
import de.jakobschaefer.htma.routing.HtmaParams
import org.apache.commons.jexl3.JexlContext
import org.slf4j.LoggerFactory
import org.thymeleaf.context.IExpressionContext
import java.util.Locale

internal class HtmaJexlContext(
  val ctx: IExpressionContext,
) : JexlContext {

  val locale: Locale
    get() = get("locale") as Locale

  val params: HtmaParams
    get() = get("params") as HtmaParams

  val messageFormatter: HtmaMessageFormatter
    get() = ctx.messageFormatter

  val htmaState: HtmaState
    get() = ctx.htma

  override fun get(name: String): Any? {
    return ctx.getVariable(name)
  }

  override fun has(name: String): Boolean {
    return ctx.containsVariable(name)
  }

  override fun set(name: String, value: Any?) {
    throw UnsupportedOperationException("JexlContext is read-only.")
  }

  companion object {
    private val log = LoggerFactory.getLogger(HtmaJexlContext::class.java)
  }
}
