package de.jakobschaefer.htma.thymeleaf.dialect

import de.jakobschaefer.htma.thymeleaf.context.htma
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class HtmaBootstrapProcessor(dialectPrefix: String) : AbstractElementTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  "title",
  false,
  null,
  false,
  1000
) {
  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    structureHandler: IElementTagStructureHandler
  ) {
    val htma = context.htma
    val childModel = context.modelFactory.createModel()
    for (js in htma.vite.mainJsModules) {
      childModel.add(context.modelFactory.createOpenElementTag(
        "script",
        mapOf(
          "type" to "module",
          "src" to js
        ),
        AttributeValueQuotes.DOUBLE,
        false))
      childModel.add(context.modelFactory.createCloseElementTag("script"))
    }
    for (css in htma.vite.mainCssModules) {
      childModel.add(
        context.modelFactory.createStandaloneElementTag(
          "link",
          mapOf("rel" to "stylesheet", "href" to css),
          AttributeValueQuotes.DOUBLE,
          false,
          false)
      )
    }
    structureHandler.insertBefore(childModel)
  }
}
