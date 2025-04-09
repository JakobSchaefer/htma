package de.jakobschaefer.htma.rendering.jexl

import com.damnhandy.uri.template.UriTemplate

internal class HtmaUrlNamespace(
  val context: HtmaContext
) {
  fun template(uriTemplate: String): String {
    return template(uriTemplate, context.params)
  }
  fun template(uriTemplate: String, params: Map<String, Any>): String {
    return UriTemplate.fromTemplate(uriTemplate).expand(params)
  }
}
