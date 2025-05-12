package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.rendering.htma
import de.jakobschaefer.htma.rendering.state.HtmaOutletSwap
import de.jakobschaefer.htma.rendering.thymeleaf.evaluateJexl
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode
import org.unbescape.html.HtmlEscape

class HtmaNavigateAttributeProcessor(
  precedence: Int,
  dialectPrefix: String,
) : AbstractAttributeTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  null,
  false,
  "navigate",
  true,
  precedence,
  true
) {
  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    attributeName: AttributeName,
    attributeValue: String,
    structureHandler: IElementTagStructureHandler
  ) {
    // The current page becomes the from-page for the rendered anchors
    val from = context.htma.toPage
    when (val attributeResult = context.evaluateJexl(attributeValue)) {
      is String -> {

        // We must detect the to-page from the rendered url
        val to = context.htma.appManifest.findPageByPath(attributeResult)
        val swap = HtmaOutletSwap.build(from, to)

        val safeAttributeValue = HtmlEscape.escapeHtml5(attributeResult)
        if (tag.elementCompleteName == "a") {
          structureHandler.setAttribute("href", safeAttributeValue)
        } else if (tag.elementCompleteName == "form") {
          structureHandler.setAttribute("action", safeAttributeValue)
          structureHandler.setAttribute("method", "get")
        } else {
          throw IllegalArgumentException("Attribute ${attributeName.attributeName} has to be used with <a> or <form> tag")
        }
        structureHandler.setAttribute("hx-get", HtmlEscape.escapeHtml5(safeAttributeValue))
        structureHandler.setAttribute("hx-push-url", "true")
        if(swap.innerMostCommonOutlet == "__root") {
          structureHandler.setAttribute("hx-target", "body")
          structureHandler.setAttribute("hx-swap", "innerHTML")
        } else {
          val targetSelector = "#" + swap.innerMostCommonOutlet
            .replace(".", "\\.")
            .replace("/", "\\/")
            .replace("{", "\\{")
            .replace("}", "\\}")
          structureHandler.setAttribute("hx-target", targetSelector)
          structureHandler.setAttribute("hx-select", targetSelector)
          structureHandler.setAttribute("hx-swap", "outerHTML")
        }
      }
      else -> throw IllegalArgumentException("Attribute ${attributeName.attributeName} has to be of type String")
    }
  }
}
