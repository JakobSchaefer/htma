package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.HtmaRenderContext
import de.jakobschaefer.htma.serde.JsonConverter
import kotlinx.serialization.encodeToString
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.unbescape.html.HtmlEscape

data class HtmaNavigationExpression(
  val path: String,
  val target: String,
  val transition: Boolean,
  val service: String?,
  val operation: String?
)

/**
 * This function sets the htmx attributes according to the template e.g. <a th:navigate="path=@{/}">link</a>
 * will lead to something like <a href="/" hx-get="/" hx-push-url="true" ...etc >link</a>
 */
fun setHtmxAttributes(
  isPost: Boolean,
  context: ITemplateContext,
  tag: IProcessableElementTag,
  structureHandler: IElementTagStructureHandler,
  expr: HtmaNavigationExpression
) {
  val destination = HtmlEscape.escapeHtml5(expr.path)
  if (tag.elementDefinition.elementName.elementName == "a") {
    structureHandler.setAttribute("href", destination)
  }
  if (isPost) {
    structureHandler.setAttribute("hx-post", destination)
  } else {
    structureHandler.setAttribute("hx-get", destination)
  }
  structureHandler.setAttribute("hx-push-url", destination)
  structureHandler.setAttribute("hx-target", expr.target)
  if (expr.target != "body") {
    // NOTE: HTMX expects the payload from the server in the body element.
    // The hx-select="body" attribute will break the functionality because HTMX will try to find a
    // body element inside the body element.
    structureHandler.setAttribute("hx-select", expr.target)
  }
  val transition = if (expr.transition) {
    " transition:true"
  } else {
    ""
  }
  structureHandler.setAttribute("hx-swap", "outerHTML${transition}")

  // Some values we will need when the user clicks the link, e.i. the "client context"
  val clientContext = HtmaNavigationClientContext(
    target = expr.target,
    service = expr.service,
    operation = expr.operation,
  )
  structureHandler.setAttribute(
    "hx-vals",
    HtmlEscape.escapeHtml5(
      JsonConverter.encodeToString(clientContext)))
  val renderContext = HtmaRenderContext.fromContext(context)

  // Mark navigation elements with the same target for out of band swaps.
  if (expr.target != "body") {
    // NOTE: When body is targeted no oob swap can happen.
    //  HTMX will remove those elements from the response document in order to swap them, but the
    // user intents to replace the whole page.
    //  The user would end up with missing elements.
    if (renderContext.clientContext != null &&
      renderContext.clientContext.target == expr.target) {
      structureHandler.setAttribute("hx-swap-oob", "true")
    }
  }
}
