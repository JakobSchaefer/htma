package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppManifestPage(
  val filePath: String,
  val remotePath: String,
  val canonicalPath: String,
  val templateName: String,
  val outletChain: Map<String, String>,
) {

  val outletChainList by lazy {
    buildOutletChainListStartingFrom("__root")
  }
  val outletChainListReversed by lazy {
    outletChainList.reversed()
  }

  fun buildOutletChainListStartingFrom(outlet: String): List<String> {
    return buildList {
      var currentOutlet: String? = outlet
      while (currentOutlet != null) {
        add(currentOutlet)
        currentOutlet = outletChain[currentOutlet]
      }
    }
  }
}
