package de.jakobschaefer.htma.routing

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

/**
 * Syntax full page navigation
 * th:navigate="path=@{/site}"
 *
 * Syntax partial swap with transition
 * th:navigate="path=@{/site},target='main',transition=true"
 */
class HtmaNavigateAttributeProcessor(dialectPrefix: String) :
    AbstractAttributeTagProcessor(
        TemplateMode.HTML, dialectPrefix, null, false, "navigate", true, 10_000, true) {
  override fun doProcess(
      context: ITemplateContext,
      tag: IProcessableElementTag,
      attributeName: AttributeName,
      attributeValue: String,
      structureHandler: IElementTagStructureHandler
  ) {
    val expr = HtmaNavigationExpressionHelper.parseExpression(attributeValue, context)
    setHtmxAttributes(false, context, tag, structureHandler, expr)
  }
}
