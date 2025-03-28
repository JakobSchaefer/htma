package de.jakobschaefer.htma.thymeleaf.dialect

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class HtmaFragmentAttributeProcessor(dialectPrefix: String) : AbstractAttributeTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  null,
  false,
  "fragment",
  true,
  1000,
  false
) {
  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    attributeName: AttributeName,
    attributeValue: String?,
    structureHandler: IElementTagStructureHandler
  ) {
    if (attributeValue == "outlet") {
      structureHandler.setAttribute("id", context.templateData.template)
    }
  }
}
