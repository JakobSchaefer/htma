package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.rendering.thymeleaf.evaluateJexl
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeNames
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.processor.AbstractProcessor
import org.thymeleaf.processor.element.IElementModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.processor.element.MatchingAttributeName
import org.thymeleaf.processor.element.MatchingElementName
import org.thymeleaf.templatemode.TemplateMode

class HtmaElemAttributeProcessor(
  dialectPrefix: String,
  precedence: Int
) : AbstractProcessor(TemplateMode.HTML, precedence), IElementModelProcessor {
  override fun process(
    context: ITemplateContext,
    model: IModel,
    structureHandler: IElementModelStructureHandler
  ) {
    val openTag = model.get(0) as IOpenElementTag
    val elemAttribute = openTag.getAttribute(attributeName)
    val attributeValue = elemAttribute.value
    val attributeResult = context.evaluateJexl(attributeValue)
    when (attributeResult) {
      is String -> {
        val attributes = buildMap<String, String> {
          for (existingAttribute in openTag.allAttributes) {
            if (attributeMatch.matches(existingAttribute.attributeDefinition.attributeName)) continue
            put(existingAttribute.attributeDefinition.attributeName.attributeName, existingAttribute.value)
          }
        }
        val newOpenTag = context.modelFactory.createOpenElementTag(attributeResult, attributes, AttributeValueQuotes.DOUBLE, false)
        val newCloseTag = context.modelFactory.createCloseElementTag(attributeResult)
        model.replace(0, newOpenTag)
        model.replace(model.size() - 1, newCloseTag)
      }
      else -> throw IllegalArgumentException("Attribute ${attributeName.attributeName} has to be of type String")
    }
  }

  override fun getMatchingElementName(): MatchingElementName? {
    return null
  }

  private val attributeName = AttributeNames.forHTMLName(dialectPrefix, "elem")
  private val attributeMatch = MatchingAttributeName.forAttributeName(
    templateMode,
    attributeName
  )
  override fun getMatchingAttributeName(): MatchingAttributeName? {
    return attributeMatch
  }
}
