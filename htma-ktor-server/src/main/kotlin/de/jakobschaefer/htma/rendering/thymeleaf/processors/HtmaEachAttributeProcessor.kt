package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.rendering.thymeleaf.evaluateJexl
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class HtmaEachAttributeProcessor(
  dialectPrefix: String,
  precedence: Int
) : AbstractAttributeTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  null,
  false,
  "each",
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
    val attributeResult = context.evaluateJexl(attributeValue)
    when (attributeResult) {
      is Iterable<*> -> {
        structureHandler.iterateElement("it", "iter",attributeResult)
      }
      is Array<*> -> {
        structureHandler.iterateElement("it", "iter",attributeResult)
      }
      else -> throw IllegalArgumentException("Attribute ${attributeName.attributeName} has to be of type Iterable<*>")
    }
  }
}
