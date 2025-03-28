package de.jakobschaefer.htma.thymeleaf.dialect

import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.dialect.IExpressionObjectDialect
import org.thymeleaf.expression.IExpressionObjectFactory
import org.thymeleaf.processor.IProcessor

class HtmaDialect : AbstractProcessorDialect("HTMA", "th", 1000), IExpressionObjectDialect {
  override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
    return setOf(
      HtmaBootstrapProcessor(dialectPrefix),
      HtmaOutletTagProcessor(dialectPrefix),
      HtmaFragmentAttributeProcessor(dialectPrefix),
      HtmaNavigateProcessor(dialectPrefix),
    )
  }

  override fun getExpressionObjectFactory(): IExpressionObjectFactory {
    return HtmaExpressionObjectFactory()
  }
}

