package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.rendering.thymeleaf.evaluateJexl
import de.jakobschaefer.htma.rendering.thymeleaf.jexl
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeNames
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.AbstractProcessor
import org.thymeleaf.processor.element.IElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.processor.element.MatchingAttributeName
import org.thymeleaf.processor.element.MatchingElementName
import org.thymeleaf.templatemode.TemplateMode

class HtmaFallbackAttributeProcessor(
  precedence: Int,
  val dialectPrefix: String
) : AbstractProcessor(
  TemplateMode.HTML,
  precedence,
), IElementTagProcessor {
  private val attributeMatch = MatchingAttributeName.forAllAttributesWithPrefix(templateMode, dialectPrefix)
  override fun getMatchingElementName(): MatchingElementName? {
    return null
  }

  override fun getMatchingAttributeName(): MatchingAttributeName? {
    return attributeMatch
  }

  override fun process(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    structureHandler: IElementTagStructureHandler
  ) {
    for (attribute in tag.allAttributes) {
      val attributeName = attribute.attributeDefinition.attributeName
      if (attributeName.isPrefixed && attributeName.prefix == dialectPrefix) {
        if (attributeName.prefix == dialectPrefix) {
          val attributeValue = attribute.value
          val attributeResult = context.evaluateJexl(attributeValue)
          when (attributeResult) {
            is String -> structureHandler.setAttribute(attributeName.attributeName, attributeResult)
            is Boolean -> if (attributeResult) {
              structureHandler.setAttribute(attributeName.attributeName, null)
            }
            else -> throw IllegalArgumentException("Attribute ${attributeName.attributeName} has to be of type String or Boolean")
          }
          structureHandler.removeAttribute(attributeName)
        }
      }
    }
  }
}
