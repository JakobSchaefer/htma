package de.jakobschaefer.htma.graphql

import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.api.Query
import de.jakobschaefer.htma.HtmaRenderContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode
import kotlin.reflect.full.primaryConstructor

// syntax:
// <variableName> = ~{ <ServiceName> :: <OperationName> }
class HtmaMutationAttributeProcessor(dialectPrefix: String) :
    AbstractAttributeTagProcessor(
        TemplateMode.HTML, dialectPrefix, null, false, "mutation", true, 10_000, true) {
  override fun doProcess(
      context: ITemplateContext,
      tag: IProcessableElementTag,
      attributeName: AttributeName,
      attributeValue: String,
      structureHandler: IElementTagStructureHandler
  ) {
    val htma = HtmaRenderContext.fromContext(context)
    val mutations = GraphQlExpressionHelper.parseGraphQlExpression(attributeValue, context)
      .assignments
      .mapValues { (_, mutationRef) ->
        for ((ref, cacheValue) in htma.graphqlCache) {
          if (ref.serviceName == mutationRef.serviceName && ref.operationName == mutationRef.operationName) {
            return@mapValues cacheValue
          }
        }
        return@mapValues GraphQlResponse(
          success = false,
          data = null,
          errors = emptyList(),
          performed = false
        )
      }
    for (mutation in mutations) {
      structureHandler.setLocalVariable(mutation.key, mutation.value)
    }
  }
}
