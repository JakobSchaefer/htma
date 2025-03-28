package de.jakobschaefer.htma.thymeleaf.dialect

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class HtmaOutletTagProcessor(dialectPrefix: String) : AbstractElementTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  "outlet",
  true,
  null,
  true,
  1000,
) {
  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    structureHandler: IElementTagStructureHandler
  ) {
    val replaceExpression = "~{ ${'$'}{htma.toPage.outlets[#execInfo.templateName]} :: outlet }"
    val mf = context.modelFactory
    val model = mf.createModel()
    model.add(mf.createOpenElementTag("div", "th:replace", replaceExpression))
    model.add(mf.createCloseElementTag("div"))
    structureHandler.replaceWith(model, true)
  }
}
