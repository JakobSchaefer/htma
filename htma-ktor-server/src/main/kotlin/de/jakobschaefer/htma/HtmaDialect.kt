package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.HtmaMutationAttributeProcessor
import de.jakobschaefer.htma.routing.HtmaNavigateAttributeProcessor
import de.jakobschaefer.htma.graphql.HtmaQueryAttributeProcessor
import de.jakobschaefer.htma.routing.HtmaPerformAttributeProcessor
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor

class HtmaDialect : AbstractProcessorDialect("HTMA", "th", 100) {
  override fun getProcessors(dialectPrefix: String): MutableSet<IProcessor> {
    return mutableSetOf(
      HtmaNavigateAttributeProcessor(dialectPrefix),
      HtmaPerformAttributeProcessor(dialectPrefix),
      HtmaQueryAttributeProcessor(dialectPrefix),
      HtmaMutationAttributeProcessor(dialectPrefix)
    )
  }
}
