package de.jakobschaefer.htma.rendering

import com.damnhandy.uri.template.UriTemplate
import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.linkbuilder.AbstractLinkBuilder

class HtmaLinkBuilder : AbstractLinkBuilder() {
  override fun buildLink(
    context: IExpressionContext,
    base: String,
    parameters: Map<String, Any?>
  ): String {
    return UriTemplate.fromTemplate(base).expand(parameters)
  }
}
