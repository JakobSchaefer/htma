package de.jakobschaefer.htma.rendering.jexl

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlFeatures
import org.apache.commons.jexl3.introspection.JexlPermissions

object HtmaJexl {
  fun build() = JexlBuilder()
    .permissions(JexlPermissions.UNRESTRICTED)
    .features(
      JexlFeatures()
      .script(false)
      .loops(false)
      .sideEffect(false)
      .sideEffectGlobal(false)
    )
    .namespaces(
      mapOf(
        "t" to HtmaTNamespace::class.java,
        "uri" to HtmaUriNamespace::class.java,
        "money" to HtmaMoneyNamespace(),
      )
    )
    .create()
}
