package de.jakobschaefer.htma.rendering.thymeleaf.processors

import com.google.gson.GsonBuilder
import de.jakobschaefer.htma.rendering.jexl.HtmaFormattedMessage
import de.jakobschaefer.htma.rendering.thymeleaf.evaluateJexl
import de.jakobschaefer.htma.rendering.thymeleaf.messageFormatter
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeNames
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.processor.element.AbstractElementModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.processor.element.MatchingAttributeName
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.util.FastStringWriter

class HtmaTextAttributeProcessor(
  dialectPrefix: String,
  precedence: Int
) : AbstractElementModelProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  null,
  false,
  "text",
  true,
  precedence
) {
  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val attributeName = AttributeNames.forHTMLName(dialectPrefix, "text")
  private val attributeMatch = MatchingAttributeName.forAttributeName(TemplateMode.HTML, attributeName)

  override fun doProcess(
    context: ITemplateContext,
    model: IModel,
    structureHandler: IElementModelStructureHandler
  ) {
    val openTag = model.get(0) as IOpenElementTag
    val attributeValue = openTag.getAttributeValue(attributeName)

    val newAttributes = openTag.attributeMap.filterKeys { key ->
      val attribute = AttributeNames.forHTMLName(key)
      !attributeMatch.matches(attribute)
    }
    val newOpenTag = context.modelFactory.createOpenElementTag(
      openTag.elementCompleteName,
      newAttributes,
      AttributeValueQuotes.DOUBLE,
      false
    )
    val newClosingTag = context.modelFactory.createCloseElementTag(openTag.elementCompleteName)
    val newText = when (val attributeResult = context.evaluateJexl(attributeValue)) {
      is String -> context.modelFactory.createText(attributeResult)
      is HtmaFormattedMessage -> {
        val innerHTML = context.modelFactory.createModel()
        for (index in 1 until model.size() - 1) {
          innerHTML.add(model.get(index))
        }

        val writer = FastStringWriter()
        innerHTML.write(writer)
        val formattedMessage = context.messageFormatter.format(
          locale = context.locale,
          pattern = writer.toString(),
          params = attributeResult.params,
        )
        context.modelFactory.createText(formattedMessage)
      }
      else -> context.modelFactory.createText(gson.toJson(attributeResult))
    }
    model.reset()
    model.add(newOpenTag)
    model.add(newText)
    model.add(newClosingTag)
  }

//  override fun doProcess(
//    context: ITemplateContext,
//    tag: IProcessableElementTag,
//    attributeName: AttributeName,
//    attributeValue: String,
//    structureHandler: IElementTagStructureHandler
//  ) {
//    val attributeResult = context.evaluateJexl(attributeValue)
//    when (attributeResult) {
//      is String -> structureHandler.setBody(HtmlEscape.escapeHtml5(attributeResult), false)
//      else -> structureHandler.setBody(HtmlEscape.escapeHtml5(gson.toJson(attributeResult)), false)
//    }
//  }
}
