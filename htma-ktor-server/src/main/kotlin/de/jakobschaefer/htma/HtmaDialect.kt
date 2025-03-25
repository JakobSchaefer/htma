package de.jakobschaefer.htma

import de.jakobschaefer.htma.thymeleaf.HtmaContext
import de.jakobschaefer.htma.thymeleaf.HtmaRenderContext
import de.jakobschaefer.htma.thymeleaf.htma
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.AttributeValueQuotes
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.IProcessor
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.AbstractElementTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class HtmaDialect : AbstractProcessorDialect("HTMA", "th", 1000) {
  override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
    return setOf(
      HtmaBootstrapProcessor(dialectPrefix),
      HtmaOutletTagProcessor(dialectPrefix),
      HtmaPlugAttributeProcessor(dialectPrefix),
    )
  }
}

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

class HtmaPlugAttributeProcessor(dialectPrefix: String) : AbstractAttributeTagProcessor(
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
