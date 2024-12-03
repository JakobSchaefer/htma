package de.jakobschaefer.htma.graphql

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.standard.expression.StandardExpressions

object GraphQlExpressionHelper {
  fun parseGraphQlExpression(expr: String, context: ITemplateContext): Map<String, GraphQlOperationRef> {
    return getVariableAssignments(expr)
      .mapValues { (_, operationString) -> computeOperation(operationString, context) }
  }

  fun getVariableAssignments(str: String): Map<String, String> {
    return str.split(',')
      .map {
        val key = it.substringBefore('=').trim()
        val value = it.substringAfter('=').trim()
        key to value
      }
      .toMap()
  }

  fun computeOperation(queryString: String, context: ITemplateContext): GraphQlOperationRef {
    val parser = StandardExpressions.getExpressionParser(context.configuration)
    val fragmentString = queryString.substringAfter("~{").substringBeforeLast('}')
    val serviceName = fragmentString.substringBefore("::").trim()
    val queryExpression = fragmentString.substringAfter("::").trim()
    val hasParameters = queryExpression.contains("(")
    val queryName =
      if (hasParameters) {
        queryExpression.substringBefore('(')
      } else {
        queryExpression
      }
    val variables =
      if (hasParameters) {
        queryExpression.substringAfter('(').substringBeforeLast(')').split(',').windowed(2, 2).associate { variable ->
          val key = parser.parseExpression(context, variable[0]).execute(context) as String
          val value = parser.parseExpression(context, variable[1]).execute(context)
          key to value
        }
      } else {
        emptyMap()
      }
    return GraphQlOperationRef(
      serviceName = serviceName,
      operationName = queryName,
      variables = variables,
    )
  }
}
