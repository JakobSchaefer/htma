package de.jakobschaefer.htma.rendering

import com.damnhandy.uri.template.UriTemplate

class HtmaContextUrlNamespace {
  fun template(uriTemplate: String): String {
    return template(uriTemplate, emptyMap())
  }
  fun template(uriTemplate: String, params: Map<String, Any>): String {
    return UriTemplate.fromTemplate(uriTemplate).expand(params)
  }
}
