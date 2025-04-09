package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppManifestPage(
  val filePath: String,
  val remotePath: String,
  val canonicalPath: String,
  val outletChain: Map<String, String>,
) {

  val outletChainList by lazy {
    var currentOutlet: String? = "__root"
    val resultList = mutableListOf<String>()
    while (currentOutlet != null) {
      resultList.add(currentOutlet!!)
      currentOutlet = outletChain[currentOutlet]
    }
    resultList
  }
  val outletChainListReversed by lazy {
    outletChainList.reversed()
  }
}
