package de.jakobschaefer.htma.rendering.thymeleaf

import de.jakobschaefer.htma.messages.HtmaMessageFormatter
import de.jakobschaefer.htma.rendering.jexl.HtmaJexl
import de.jakobschaefer.htma.rendering.jexl.HtmaJexlContext
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaClassAppendAttributeProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaEachAttributeProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaFallbackAttributeProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaElemAttributeProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaNavigateAttributeProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaOutletElementProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaTextAttributeProcessor
import de.jakobschaefer.htma.rendering.thymeleaf.processors.HtmaWebComponentProcessor
import de.jakobschaefer.htma.webinf.AppManifest
import org.apache.commons.jexl3.JexlEngine
import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.dialect.IExecutionAttributeDialect
import org.thymeleaf.processor.IProcessor

internal class HtmaDialect(
  val messageFormatter: HtmaMessageFormatter,
  val appManifest: AppManifest
) : AbstractProcessorDialect("Htma", "x", 10), IExecutionAttributeDialect {
  override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
    return buildSet {
      add(HtmaOutletElementProcessor(precedence = 1_000))

      add(HtmaEachAttributeProcessor(precedence = 2_000, dialectPrefix = dialectPrefix))

      add(HtmaTextAttributeProcessor(precedence = 3_000, dialectPrefix = dialectPrefix))
      add(HtmaClassAppendAttributeProcessor(precedence = 4_000, dialectPrefix = dialectPrefix))
      add(HtmaElemAttributeProcessor(precedence = 5_000, dialectPrefix = dialectPrefix))

      add(HtmaNavigateAttributeProcessor(precedence = 5_000, dialectPrefix = dialectPrefix))

      add(
        HtmaFallbackAttributeProcessor(
          precedence = 10_000,
          dialectPrefix = dialectPrefix
        )
      )

      for (webComponent in appManifest.webComponents) {
        add(
          HtmaWebComponentProcessor(
            precedence = 20_000,
            dialectPrefix = dialectPrefix,
            componentName = webComponent.name,
            templateName = webComponent.templateName
          )
        )
      }

    }
  }

  override fun getExecutionAttributes(): Map<String, Any> {
    return mapOf(
      HTMA_DIALECT_EXECUTION_ATTRIBUTE_JEXL to HtmaJexl.build(),
      HTMA_DIALECT_EXECUTION_ATTRIBUTE_MESSAGE_FORMATTER to messageFormatter,
    )
  }
}

const val HTMA_DIALECT_EXECUTION_ATTRIBUTE_JEXL = "jexl"
internal val IExpressionContext.jexl: JexlEngine
  get() = configuration.executionAttributes[HTMA_DIALECT_EXECUTION_ATTRIBUTE_JEXL] as JexlEngine

const val HTMA_DIALECT_EXECUTION_ATTRIBUTE_MESSAGE_FORMATTER = "messageFormatter"
internal val IExpressionContext.messageFormatter: HtmaMessageFormatter
  get() = configuration.executionAttributes[HTMA_DIALECT_EXECUTION_ATTRIBUTE_MESSAGE_FORMATTER] as HtmaMessageFormatter

fun IExpressionContext.evaluateJexl(expression: String): Any {
  val expr = jexl.createExpression(expression)
  return expr.evaluate(HtmaJexlContext(this))
}
