package de.jakobschaefer.htma.graphql

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.standard.expression.StandardExpressions

object GraphQlExpressionHelper {
  fun parseGraphQlExpression(expr: String, context: ITemplateContext): GraphQlExpression {
    val thStdParser = StandardExpressions.getExpressionParser(context.configuration)
    val parsedExpression = GraphQlExpressionGrammar.parseToEnd(expr)
    return parsedExpression.copy(
      assignments = parsedExpression.assignments.mapValues { (_, operationRef) ->
        operationRef.copy(
          variables = operationRef.variables.mapValues { (_, value) ->
            val thExpr = value as String
            thStdParser.parseExpression(context, thExpr).execute(context)
          }
        )
      }
    )
  }
}
