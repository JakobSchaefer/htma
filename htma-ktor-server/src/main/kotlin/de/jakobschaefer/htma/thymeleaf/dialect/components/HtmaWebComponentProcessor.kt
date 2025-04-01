package de.jakobschaefer.htma.thymeleaf.dialect.components

import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.IModel
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.processor.element.AbstractElementModelProcessor
import org.thymeleaf.processor.element.IElementModelStructureHandler
import org.thymeleaf.standard.expression.Fragment
import org.thymeleaf.standard.expression.FragmentExpression
import org.thymeleaf.standard.expression.StandardExpressions
import org.thymeleaf.templatemode.TemplateMode

class HtmaWebComponentProcessor(
  dialectPrefix: String,
  private val componentName: String
) : AbstractElementModelProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  componentName,
  true,
  null,
  false,
  1,
) {
  override fun doProcess(
    context: ITemplateContext,
    model: IModel,
    structureHandler: IElementModelStructureHandler
  ) {
    val mf = context.modelFactory
    val child = mf.createModel()
    val templateTag = "${dialectPrefix}:${HtmaWebTemplateProcessor.TAG_NAME}"
    child.add(mf.createOpenElementTag(templateTag, "name", componentName))
    child.add(mf.createCloseElementTag(templateTag))

    val openTag = model[0] as IOpenElementTag
    val newOpenTag = mf.createOpenElementTag(componentName, openTag.attributeMap, AttributeValueQuotes.DOUBLE, false)
    val newClosTag = mf.createCloseElementTag(componentName)
    model.replace(0, newOpenTag)
    model.replace(model.size() - 1, newClosTag)
    model.insertModel(1, child)

    structureHandler.setLocalVariable("attributes", openTag.attributeMap)
  }
}
