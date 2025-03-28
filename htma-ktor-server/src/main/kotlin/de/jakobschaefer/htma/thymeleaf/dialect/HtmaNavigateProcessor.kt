package de.jakobschaefer.htma.thymeleaf.dialect

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.expression.StandardExpressions
import org.thymeleaf.templatemode.TemplateMode

class HtmaNavigateProcessor(dialectPrefix: String) : AbstractAttributeTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  null,
  false,
  "navigate",
  true,
  1000,
  true
) {

  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    attributeName: AttributeName,
    attributeValue: String,
    structureHandler: IElementTagStructureHandler
  ) {
    val expression = StandardExpressions.getExpressionParser(context.configuration)
      .parseExpression(context, attributeValue)
      .execute(context) as String
    if (tag.elementCompleteName == "a") {
      structureHandler.setAttribute("href", expression)
    }
    structureHandler.setAttribute("hx-get", expression)
    structureHandler.setAttribute("hx-push-url", "true")
  }
}
