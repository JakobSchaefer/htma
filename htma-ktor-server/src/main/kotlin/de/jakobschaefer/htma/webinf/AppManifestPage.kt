package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPage(
  val filePath: String,
  val remotePath: String,
  val canonicalPath: String,
  val outletChain: Map<String, String>
) {
  fun forEachOutlet(fn: (String) -> Unit) {
    var currentOutlet: String? = "__root"
    while (currentOutlet != null) {
      fn(currentOutlet)
      currentOutlet = outletChain[currentOutlet]
    }
  }
}
