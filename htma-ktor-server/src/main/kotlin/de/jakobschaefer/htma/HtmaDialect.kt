package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.HtmaQueryAttributeProcessor
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor

class HtmaDialect : AbstractProcessorDialect("HTMA", "th", 100) {
  override fun getProcessors(dialectPrefix: String): MutableSet<IProcessor> {
    return mutableSetOf(
      HtmaQueryAttributeProcessor(dialectPrefix),
    )
  }
}
