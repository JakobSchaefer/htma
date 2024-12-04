package de.jakobschaefer.htma.routing

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.standard.expression.StandardExpressions

object HtmaNavigationExpressionHelper {
  fun parseExpression(expr: String, context: ITemplateContext): HtmaNavigationExpression {
    val parser = StandardExpressions.getExpressionParser(context.configuration)
    val parsedConfig =
      expr
        .split(",").associate {
          val key = it.substringBefore('=').trim()
          val value = it.substringAfter('=').trim()
          key to value
        }
        .mapValues { (_, value) ->
          val valueExpr = parser.parseExpression(context, value)
          val parsedValue = valueExpr.execute(context)
          parsedValue
        }
    return HtmaNavigationExpression(
      path = parsedConfig["path"] as String,
      target = (parsedConfig["target"] as String?) ?: "body",
      transition = (parsedConfig["transition"] as Boolean?) ?: false,
      service = parsedConfig["service"] as String?,
      operation = parsedConfig["operation"] as String?,
    )
  }
}
