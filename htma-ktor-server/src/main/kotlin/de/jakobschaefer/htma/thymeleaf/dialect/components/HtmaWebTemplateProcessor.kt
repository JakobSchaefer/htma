package de.jakobschaefer.htma.thymeleaf.dialect.components

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.expression.Fragment
import org.thymeleaf.standard.expression.FragmentExpression
import org.thymeleaf.standard.expression.StandardExpressions
import org.thymeleaf.templatemode.TemplateMode

class HtmaWebTemplateProcessor(dialectPrefix: String) : AbstractElementTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  TAG_NAME,
  true,
  null,
  true,
  2
) {

  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    structureHandler: IElementTagStructureHandler
  ) {
    val componentName = tag.getAttribute("name").value
    val expressionParser = StandardExpressions.getExpressionParser(context.configuration)
    val fragmentExpression = expressionParser.parseExpression(context, "~{ __components/${componentName} :: template }") as FragmentExpression
    val fragment = fragmentExpression.execute(context) as Fragment

    structureHandler.replaceWith(fragment.templateModel, true)
  }

  companion object {
    const val TAG_NAME = "template"
  }
}
