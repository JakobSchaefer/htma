package de.jakobschaefer.htma.thymeleaf.dialect.components

import de.jakobschaefer.htma.webinf.AppComponent
import org.thymeleaf.dialect.AbstractProcessorDialect
import org.thymeleaf.processor.IProcessor


class HtmaWebComponentDialect(
  name: String,
  prefix: String,
  private val componentNames: List<AppComponent>
) : AbstractProcessorDialect(name, prefix, PRECEDENCE) {
  override fun getProcessors(dialectPrefix: String): Set<IProcessor> {
    return buildSet {
      add(HtmaWebTemplateProcessor(dialectPrefix))
      addAll(componentNames.map { HtmaWebComponentProcessor(dialectPrefix, it.name) })
    }
  }

  companion object {
    private const val PRECEDENCE = 2000 // Must come after the standard dialect e.i. must be higher than the standard dialect which is 1000
  }
}

