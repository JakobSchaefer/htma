package de.jakobschaefer.htma.rendering.jexl

import org.javamoney.moneta.FastMoney
import javax.money.CurrencyUnit
import javax.money.Monetary
import javax.money.MonetaryAmount

class HtmaMoneyNamespace {
  private val EUR = Monetary.getCurrency("EUR")
  private val USD = Monetary.getCurrency("USD")

  fun eur(value: Double): MonetaryAmount {
    return amount(value, EUR)
  }

  fun eur(value: Long): MonetaryAmount {
    return amount(value, EUR)
  }

  fun usd(value: Double): MonetaryAmount {
    return amount(value, EUR)
  }

  fun usd(value: Long): MonetaryAmount {
    return amount(value, EUR)
  }

  fun amount(value: Double, currency: CurrencyUnit): MonetaryAmount {
    return FastMoney.of(value, currency)
  }

  fun amount(value: Long, currency: CurrencyUnit): MonetaryAmount {
    return FastMoney.ofMinor(currency, value)
  }

  fun currency(code: String): CurrencyUnit {
    return Monetary.getCurrency(code)
  }
}
