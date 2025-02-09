package de.jakobschaefer.htma.graphql

import de.jakobschaefer.htma.HtmaRoutingCall
import org.thymeleaf.TemplateSpec
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.dialect.IPreProcessorDialect
import org.thymeleaf.engine.AbstractTemplateHandler
import org.thymeleaf.engine.ITemplateHandler
import org.thymeleaf.model.ICloseElementTag
import org.thymeleaf.model.IOpenElementTag
import org.thymeleaf.preprocessor.IPreProcessor
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.util.FastStringWriter

class GraphQlPreProcessorDialect : IPreProcessorDialect {
  override fun getName(): String {
    return "HtmaPreDialect"
  }

  override fun getDialectPreProcessorPrecedence(): Int {
    return 1000
  }

  override fun getPreProcessors(): MutableSet<IPreProcessor> {
    return mutableSetOf(QueryDataFetcher())
  }
}

class QueryDataFetcher : IPreProcessor {
  override fun getTemplateMode(): TemplateMode {
    return TemplateMode.HTML
  }

  override fun getPrecedence(): Int {
    return 1000
  }

  override fun getHandlerClass(): Class<out ITemplateHandler> {
    return QueryDataFetchHandler::class.java
  }
}

class QueryDataFetchHandler : AbstractTemplateHandler() {
  override fun handleOpenElement(openElementTag: IOpenElementTag) {
    if (openElementTag.hasAttribute("th:query")) {
      val call = HtmaRoutingCall.fromContext(context)
      val expressionString = openElementTag.getAttributeValue("th:query")
      val expression = GraphQlExpressionHelper.parseGraphQlExpression(expressionString, context)
      expression.assignments.values.forEach { operation ->
        val query = parseAndProcessGraphQlTemplate(operation.templateName, context)
        call.startGraphQlOperation(operation, query)
      }
    }
    super.handleOpenElement(openElementTag)
  }

  private fun parseAndProcessGraphQlTemplate(templateName: String, context: ITemplateContext): String {
    val stringWriter = FastStringWriter(200)
    val graphqlTemplate = TemplateSpec("${templateName}.graphql", TemplateMode.TEXT)
    context.configuration.templateManager.parseAndProcess(graphqlTemplate, context, stringWriter)
    return stringWriter.toString()
  }

  override fun handleCloseElement(closeElementTag: ICloseElementTag) {
    super.handleCloseElement(closeElementTag)
  }
}
