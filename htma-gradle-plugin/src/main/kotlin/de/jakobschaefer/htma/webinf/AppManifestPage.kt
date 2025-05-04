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
)
