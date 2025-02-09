package de.jakobschaefer.htma.graphql

import de.jakobschaefer.htma.HtmaRoutingCall
import kotlinx.coroutines.runBlocking
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

// syntax:
// <variableName> = ~{ <TemplateName> :: <QueryName>(parameters...) }
class HtmaQueryAttributeProcessor(dialectPrefix: String) :
    AbstractAttributeTagProcessor(
        TemplateMode.HTML, dialectPrefix, null, false, "query", true, 10_000, true) {
  override fun doProcess(
      context: ITemplateContext,
      tag: IProcessableElementTag,
      attributeName: AttributeName,
      attributeValue: String,
      structureHandler: IElementTagStructureHandler
  ) {
    val call = HtmaRoutingCall.fromContext(context)
    val gqlExpr = GraphQlExpressionHelper.parseGraphQlExpression(attributeValue, context)
    runBlocking {
      for ((variableName, queryOperation) in gqlExpr.assignments) {
        val result = call.awaitGraphQlOperation(queryOperation)
        structureHandler.setLocalVariable(variableName, result)
      }
    }
  }
}

