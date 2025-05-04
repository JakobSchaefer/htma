package de.jakobschaefer.htma.webinf

import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPage(
  val webPath: String,
  val remotePath: String,
  val remotePathPriority: Int,
  val canonicalPath: String,
  val templateName: String,
  val outletChain: Map<String, String>,
  val routeConfig: AppManifestPageRouteConfig
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
