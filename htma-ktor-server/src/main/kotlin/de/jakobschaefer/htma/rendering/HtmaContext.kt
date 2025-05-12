package de.jakobschaefer.htma.rendering

import de.jakobschaefer.htma.rendering.state.HtmaState
import de.jakobschaefer.htma.routing.HtmaParams
import org.thymeleaf.context.IContext
import org.thymeleaf.context.IExpressionContext
import java.util.Locale

class HtmaContext(
  locale: Locale,
  location: String,
  params: HtmaParams,
  htma: HtmaState,
  query: Map<String, Any>,
  mutation: Map<String, Any>
) : IContext {

  private val variables: MutableMap<String, Any> = mutableMapOf()

  init {
    variables["locale"] = locale
    variables["location"] = location
    variables["params"] = params
    variables["htma"] = htma
    variables["query"] = query
    variables["mutation"] = mutation
  }

  override fun getLocale(): Locale {
    return variables["locale"] as Locale
  }

  override fun containsVariable(name: String): Boolean {
    return variables.containsKey(name)
  }

  override fun getVariableNames(): Set<String> {
    return variables.keys
  }

  override fun getVariable(name: String): Any? {
    return variables[name]
  }
}

val IExpressionContext.htma: HtmaState
  get() = getVariable("htma") as HtmaState

