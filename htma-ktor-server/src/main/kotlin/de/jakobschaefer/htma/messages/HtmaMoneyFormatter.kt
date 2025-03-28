package de.jakobschaefer.htma.messages

import com.ibm.icu.message2.FormattedPlaceholder
import com.ibm.icu.message2.Formatter
import com.ibm.icu.number.Notation
import com.ibm.icu.number.NumberFormatter
import com.ibm.icu.util.Currency
import java.util.*
import javax.money.MonetaryAmount

class HtmaMoneyFormatter(
  val locale: Locale,
) : Formatter {

  override fun formatToString(toFormat: Any, variableOptions: Map<String, Any>): String {
    return format(toFormat, variableOptions).toString()
  }

  override fun format(
    toFormat: Any,
    variableOptions: Map<String, Any>
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
