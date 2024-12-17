package de.jakobschaefer.htma.graphql

import de.jakobschaefer.htma.HtmaRenderContext
import de.jakobschaefer.htma.htma
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.thymeleaf.TemplateSpec
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.util.FastStringWriter

// syntax:
// <variableName> = ~{ <TemplateName> :: <QueryName>(parameters...) }
class HtmaQueryAttributeProcessor(dialectPrefix: String) :
    AbstractAttributeTagProcessor(
        TemplateMode.HTML, dialectPrefix, null, false, "query", true, 10_000, true) {
  override fun doProcess(
      context: ITemplateContext,
      tag: IProcessableElementTag,
      attributeName: AttributeName,
      attributeValue: String,
      structureHandler: IElementTagStructureHandler
  ) {
    val htma = HtmaRenderContext.fromContext(context)
    val gqlExpr = GraphQlExpressionHelper.parseGraphQlExpression(attributeValue, context)
    val queries =
      runBlocking {
        gqlExpr.assignments
          .map { (variableName, queryRef) ->
            async {
              val stringWriter = FastStringWriter(200)
              val graphqlTemplate = TemplateSpec("${queryRef.templateName}.graphql", TemplateMode.TEXT)
              context.configuration.templateManager.parseAndProcess(graphqlTemplate, context, stringWriter)
              val query = stringWriter.toString()
              val result = htma.call.application.htma.graphqlEngine!!.execute(htma.call, queryRef, query)
              variableName to result
            }
          }.awaitAll()
      }.toMap()

    for (query in queries) {
      structureHandler.setLocalVariable(query.key, query.value)
    }
  }
}

