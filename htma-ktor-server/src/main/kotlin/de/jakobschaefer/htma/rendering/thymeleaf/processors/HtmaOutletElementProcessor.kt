package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.Logs
import de.jakobschaefer.htma.rendering.htma
import org.thymeleaf.TemplateSpec
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.ElementNames
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.ICloseElementTag
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.AbstractProcessor
import org.thymeleaf.processor.element.IElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.processor.element.MatchingAttributeName
import org.thymeleaf.processor.element.MatchingElementName
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.util.FastStringWriter

class HtmaOutletElementProcessor(
  precedence: Int
) : AbstractProcessor(
  TemplateMode.HTML,
  precedence,
), IElementTagProcessor {

  override fun process(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    structureHandler: IElementTagStructureHandler
  ) {
    val currentTemplate = context.templateData.template
    val plugTemplateName = context.htma.toPage.outletChain[currentTemplate]!!
    val plug = parseAndProcessPlug(context, plugTemplateName)

    structureHandler.replaceWith(plug, true)
  }

  fun parseAndProcessPlug(context: ITemplateContext, templateName: String): IModel {
    val templateSpec = TemplateSpec(templateName, TemplateMode.HTML)
    val writer = FastStringWriter()
    context.configuration.templateManager.parseAndProcess(
      templateSpec,
      context,
      writer
    )
    val mf = context.modelFactory
    val loadedPlug = mf.parse(context.templateData, writer.toString())
    val plug = mf.createModel()
    var isReadingPlug = false
    for (i in 0 until loadedPlug.size()) {
      val event = loadedPlug.get(i)
      when (event) {
        is IOpenElementTag -> {
          if (isReadingPlug) {
            plug.add(event)
          } else if (event.elementDefinition.elementName.elementName == "plug") {
            isReadingPlug = true
            val openTag = mf.createOpenElementTag(
              "div",
              event.attributeMap + Pair("id", templateName),
              AttributeValueQuotes.DOUBLE, false)
            plug.add(openTag)
          }
        }
        is ICloseElementTag -> {
          if (event.elementDefinition.elementName.elementName == "plug") {
            plug.add(mf.createCloseElementTag("div"))
            break;
          } else if (isReadingPlug) {
            plug.add(event)
          }
        }
        else -> {
          if (isReadingPlug) {
            plug.add(event)
          }
        }
      }
    }

    if (!isReadingPlug) {
      Logs.htma.warn("Plug not found for template $templateName")
      plug.add(mf.createText("??$templateName??"))
    }

    return plug
  }

  private val elementMatch = MatchingElementName.forElementName(
    templateMode,
    ElementNames.forHTMLName("outlet")
  )
  override fun getMatchingElementName(): MatchingElementName? {
    return elementMatch
  }

  override fun getMatchingAttributeName(): MatchingAttributeName? {
    return null
  }

  companion object {
    fun templateNameToId(templateName: String): String {
      return templateName
        .replace('/', '-')
        .replace('.', '-')
        .replace('_', '-')
        .replace('{', '-')
        .replace('}', '-')
    }
  }
}
