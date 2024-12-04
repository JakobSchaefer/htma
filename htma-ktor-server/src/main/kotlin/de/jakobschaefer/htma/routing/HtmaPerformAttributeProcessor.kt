package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.HtmaRenderContext
import de.jakobschaefer.htma.serde.JsonConverter
import kotlinx.serialization.encodeToString
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.standard.expression.StandardExpressions
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape

/**
 * Syntax full page navigation
 * th:perform="path=@{/site},mutation=~{ <ServiceName> :: <OperationName>}"
 *
 * Syntax partial swap with transition
 * th:perform="path=@{/site},mutation=~{ <ServiceName> :: <OperationName> },target='main',transition=true"
 */
class HtmaPerformAttributeProcessor(dialectPrefix: String) :
    AbstractAttributeTagProcessor(
        TemplateMode.HTML, dialectPrefix, null, false, "perform", true, 10_000, true) {
  override fun doProcess(
      context: ITemplateContext,
      tag: IProcessableElementTag,
      attributeName: AttributeName,
      attributeValue: String,
      structureHandler: IElementTagStructureHandler
  ) {
    val expr = HtmaNavigationExpressionHelper.parseExpression(attributeValue, context)
    setHtmxAttributes(true, context, tag, structureHandler, expr)
  }
}
