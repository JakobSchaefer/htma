package de.jakobschaefer.htma.rendering.jexl

import java.util.*

internal class HtmaTNamespace(
  val context: HtmaContext
) {
  fun fmt(pattern: String): String {
    return fmt(pattern, context.params, context.locale)
  }
  fun fmt(pattern: String, params: Map<String, Any>): String {
    return fmt(pattern, params, context.locale)
  }
  fun fmt(pattern: String, params: Map<String, Any>, locale: Locale): String {
    val handyParams = buildHandyParams(params)
    return context.configuration.formatter.format(locale, pattern, handyParams)
  }
  fun msg(key: String): HtmaFormattedMessage {
    return msg(key, context.params, context.locale)
  }
  fun msg(key: String, params: Map<String, Any>): HtmaFormattedMessage {
    return msg(key, params, context.locale)
  }

  fun msg(key: String, params: Map<String, Any>, locale: Locale): HtmaFormattedMessage {
    val handyParams = buildHandyParams(params)
    return HtmaFormattedMessage(
      message = null,
      params = handyParams,
    )
  }

  private fun buildHandyParams(params: Map<String, Any>): Map<String, Any> {
    return params.mapValues { (_, v) ->
      when (v) {
        is Collection<*> -> v.first()!!
        else -> v
      }
    }
  }
}

internal class HtmaFormattedMessage(
  val message: String?,
  val params: Map<String, Any>
)
