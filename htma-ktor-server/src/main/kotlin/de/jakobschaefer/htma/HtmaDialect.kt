package de.jakobschaefer.htma

import de.jakobschaefer.htma.rendering.HtmaNavigateAttributeProcessor
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor

class HtmaDialect : AbstractProcessorDialect("HTMA", "th", 1000) {
  override fun getProcessors(dialectPrefix: String): MutableSet<IProcessor> {
    return mutableSetOf(
      HtmaNavigateAttributeProcessor(dialectPrefix),
    )
  }
}
