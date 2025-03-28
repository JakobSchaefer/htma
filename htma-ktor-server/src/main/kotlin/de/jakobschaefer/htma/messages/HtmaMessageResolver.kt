package de.jakobschaefer.htma.messages

import com.ibm.icu.message2.MFFunctionRegistry
import com.ibm.icu.message2.MessageFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.thymeleaf.messageresolver.StandardMessageResolver
import java.util.*
import javax.money.MonetaryAmount

class HtmaMessageResolver : StandardMessageResolver() {
  private val functions = MFFunctionRegistry.builder()
    .setFormatter("money") { locale, params -> HtmaMoneyFormatter(locale) }
    .setDefaultFormatterNameForType(MonetaryAmount::class.java, "money")
    .build()

  override fun formatMessage(locale: Locale, message: String, messageParameters: Array<out Any>): String {
    val icuFormatter = MessageFormatter.builder()
      .setPattern(message)
      .setFunctionRegistry(functions)
      .setLocale(locale)
      .build()
    val args = messageParameters.toList()
      .windowed(size = 2, step = 2)
      .associate { window -> Pair(window[0] as String,
        tryConvertKotlinTypeToSomethingIcu4jsBuildInFormatterCanUnderstand(window[1])
      ) }
    return icuFormatter.formatToString(args)
  }
}

private fun tryConvertKotlinTypeToSomethingIcu4jsBuildInFormatterCanUnderstand(arg: Any): Any {
  return when (arg) {
    is Instant -> Date.from(arg.toJavaInstant())
    is LocalDateTime -> Date.from(arg.toInstant(TimeZone.currentSystemDefault()).toJavaInstant())
    else -> arg
  }
}

