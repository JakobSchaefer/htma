package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.rendering.htma
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import org.thymeleaf.TemplateSpec
import org.thymeleaf.context.Context
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.ICloseElementTag
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.processor.element.AbstractElementModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.util.FastStringWriter

class HtmaWebComponentProcessor(
  precedence: Int,
  dialectPrefix: String,
  val componentName: String,
  val templateName: String
) : AbstractElementModelProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  componentName,
  false,
  null,
  false,
  precedence
) {

  override fun doProcess(
    context: ITemplateContext,
    model: IModel,
    structureHandler: IElementModelStructureHandler
  ) {
    if (!hasBeenProcessed(model)) {
      val openTag = model.get(0) as IOpenElementTag
      val template = parseAndProcessWebComponentTemplate(context, templateName, openTag.attributeMap)

      // Add stylesheets
//      for (css in context.htma.viteManifest.mainCssModules) {
//        val link = context.modelFactory.createModel()
//        val attrs = mapOf(
//          "ref" to "stylesheet",
//          "href" to css
//        )
//        link.add(context.modelFactory.createStandaloneElementTag("link", attrs, AttributeValueQuotes.DOUBLE, false, false))
//        template.insertModel(1, link)
//      }
      model.insertModel(1, template)
    }
  }

  private fun hasBeenProcessed(model: IModel): Boolean {
    for (index in 0 until model.size()) {
      val event = model.get(index)
      if (event is IOpenElementTag && event.elementCompleteName == "template" && event.hasAttribute("shadowrootmode")) {
        return true
      }
    }
    return false
  }

  private fun parseAndProcessWebComponentTemplate(context: ITemplateContext, templateName: String, attributes: Map<String, String>): IModel {
    val writer = FastStringWriter()
    val templateSpec = TemplateSpec(templateName, TemplateMode.HTML)
    val webComponentContext = Context()
    webComponentContext.setVariable("attributes", attributes)
    webComponentContext.setVariable("htma", context.htma)
    context.configuration.templateManager.parseAndProcess(templateSpec, webComponentContext, writer)
    val webComponentHtml = context.modelFactory.parse(context.templateData, writer.toString())
    val templateModel = context.modelFactory.createModel()
    var isReadingTemplate = false
    for (i in 0 until webComponentHtml.size()) {
      when (val event = webComponentHtml.get(i)) {
        is IOpenElementTag -> {
          if (isReadingTemplate) {
            templateModel.add(event)
          } else if (event.elementCompleteName == "template") {
            isReadingTemplate = true
            templateModel.add(event)
          }
        }
        is ICloseElementTag -> {
          if (event.elementCompleteName == "template") {
            templateModel.add(event)
            break
          } else if (isReadingTemplate) {
            templateModel.add(event)
          }
        }
        else -> {
          if (isReadingTemplate) {
            templateModel.add(event)
          }
        }
      }
    }

    if (!isReadingTemplate) {
      throw IllegalArgumentException("WebComponent template not found for name $templateName")
    }

    return templateModel
  }
}
