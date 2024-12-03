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
    val parser = StandardExpressions.getExpressionParser(context.configuration)
    val parsedConfig =
      attributeValue
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
    val config =
        object {
          val path: String by parsedConfig
          val target: String
            get() = (parsedConfig["target"] as String?) ?: "body"
          val transition: Boolean
            get() = (parsedConfig["transition"] as Boolean?) ?: false
          val mutation: String by parsedConfig
        }
    val destination = HtmlEscape.escapeHtml5(config.path)

    structureHandler.setAttribute("hx-post", destination)
//    structureHandler.setAttribute("hx-push-url", destination)
    structureHandler.setAttribute("hx-target", config.target)
    if (config.target != "body") {
      // NOTE: HTMX expects the payload from the server in the body element.
      // The hx-select="body" attribute will break the functionality because HTMX will try to find a
      // body element inside the body element.
      structureHandler.setAttribute("hx-select", config.target)
    }
    val transition = if (config.transition) {
      " transition:true"
    } else {
      ""
    }
    structureHandler.setAttribute("hx-swap", "outerHTML${transition}")
    structureHandler.setAttribute(
        "hx-vals",
        HtmlEscape.escapeHtml5(
            JsonConverter.encodeToString(HtmaNavigationContext(target = config.target))))
    val renderContext = HtmaRenderContext.fromContext(context)

    // Mark navigation elements with the same target for out of band swaps.
    if (config.target != "body") {
      // NOTE: When body is targeted no oob swap can happen.
      //  HTMX will remove those elements from the response document in order to swap them, but the
      // user intents to replace the whole page.
      //  The user would end up with missing elements.
      if (renderContext.isHxRequest &&
          renderContext.hxTarget != null &&
          renderContext.hxTarget == config.target) {
        structureHandler.setAttribute("hx-swap-oob", "true")
      }
    }
  }
}
