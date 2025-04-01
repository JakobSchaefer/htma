package de.jakobschaefer.htma.thymeleaf.dialect

import de.jakobschaefer.htma.thymeleaf.dialect.components.HtmaWebComponentProcessor
import de.jakobschaefer.htma.webinf.AppComponent
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.dialect.IExpressionObjectDialect
import org.thymeleaf.expression.IExpressionObjectFactory
import org.thymeleaf.processor.IProcessor

class HtmaDialect(
  name: String,
  prefix: String,
) : AbstractProcessorDialect(name, prefix, PRECEDENCE), IExpressionObjectDialect {
  override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
    return buildSet {
      add(HtmaBootstrapProcessor(dialectPrefix))
      add(HtmaOutletTagProcessor(dialectPrefix))
      add(HtmaFragmentAttributeProcessor(dialectPrefix))
      add(HtmaNavigateProcessor(dialectPrefix))
    }
  }

  override fun getExpressionObjectFactory(): IExpressionObjectFactory {
    return HtmaExpressionObjectFactory()
  }

  companion object {
    private const val PRECEDENCE = 10 // Must be lower than the standard dialect which is 1000
  }
}

