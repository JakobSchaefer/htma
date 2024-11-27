package de.jakobschaefer.htma

import com.ibm.icu.message2.FormattedPlaceholder
import com.ibm.icu.message2.Formatter
import com.ibm.icu.message2.MFFunctionRegistry
import com.ibm.icu.message2.MessageFormatter
import com.ibm.icu.number.Notation
import com.ibm.icu.number.NumberFormatter
import com.ibm.icu.util.Currency
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.thymeleaf.messageresolver.StandardMessageResolver
import java.util.*
import javax.money.MonetaryAmount

class HtmaMessageResolver : StandardMessageResolver() {
  private val functions = MFFunctionRegistry.builder()
    .setFormatter("money") { locale, params -> HtmaMoneyFormatter(locale)}
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
      .associate { window -> Pair(window[0] as String, tryConvertKotlinTypeToSomethingIcu4jsBuildInFormatterCanUnderstand(window[1])) }
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

private class HtmaMoneyFormatter(
  val locale: Locale,
) : Formatter {

  override fun formatToString(toFormat: Any, variableOptions: MutableMap<String, Any>): String {
    return format(toFormat, variableOptions).toString()
  }

  override fun format(
    toFormat: Any,
    variableOptions: MutableMap<String, Any>
  ): FormattedPlaceholder {
    return when (toFormat) {
      is MonetaryAmount -> {
        val currencyCode = toFormat.currency.currencyCode
        val amount = toFormat.number.doubleValueExact()
        val format = NumberFormatter.withLocale(locale)
          .notation(Notation.simple())
          .unit(Currency.getInstance(currencyCode))
          .format(amount)
        FormattedPlaceholder(toFormat, format)
      }
      else -> throw IllegalArgumentException("Money formatter only accepts javax.money.MonetaryAmount, but got ${toFormat::class.qualifiedName}")
    }
  }
}
