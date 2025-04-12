package de.jakobschaefer.htma.messages

import com.ibm.icu.message2.MFFunctionRegistry
import com.ibm.icu.message2.MessageFormatter
import java.util.*
import javax.money.MonetaryAmount

internal class HtmaFormatter {
  private val functions = MFFunctionRegistry.builder()
    .setFormatter("money") { locale, params -> HtmaMoneyFormatter(locale) }
    .setDefaultFormatterNameForType(MonetaryAmount::class.java, "money")
    .build()

  fun format(locale: Locale, pattern: String, params: Map<String, Any?>): String {
    val icuFormatter = MessageFormatter.builder()
      .setFunctionRegistry(functions)
      .setPattern(pattern)
      .setLocale(locale)
      .build()
    return icuFormatter.formatToString(params)
  }
}
