package de.jakobschaefer.htma.thymeleaf.dialect

import org.javamoney.moneta.FastMoney
import org.javamoney.moneta.Money
import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.expression.IExpressionObjectFactory
import javax.money.MonetaryAmount

class HtmaExpressionObjectFactory : IExpressionObjectFactory {
  override fun getAllExpressionObjectNames(): Set<String> {
    return setOf("money")
  }

  override fun buildObject(context: IExpressionContext, expressionObjectName: String): Any {
    return MoneyExpressionObject()
  }

  override fun isCacheable(expressionObjectName: String): Boolean {
    return true
  }
}

class MoneyExpressionObject {
  fun eur(value: Double) : MonetaryAmount {
    return FastMoney.of(value, "EUR")
  }
  fun usd(value: Double) : MonetaryAmount {
    return FastMoney.of(value, "USD")
  }
}
