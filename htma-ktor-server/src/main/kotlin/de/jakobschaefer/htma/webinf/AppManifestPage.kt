package de.jakobschaefer.htma.webinf

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class AppManifestPage(
  /**
   * The path within the web folder.
   */
  val webPath: String,

  /**
   * The ktor routing path.
   */
  val remotePath: String,

  /**
   * Some paths map to the same remotePath. We need to prioritize the correct path.
   */
  val remotePathPriority: Int,

  /**
   * The canonical path of the page. The remote path and the outlet chain are deduced from this.
   */
  val canonicalPath: String,

  /**
   * The template name of the page. Used to look up the template file.
   */
  val templateName: String,

  /**
   * The template names of the outlets that constitute the page.
   */
  val outletChain: Map<String, String>,

  /**
   * The route configuration defined in the head block of the page.
   */
  val routeConfig: AppManifestPageRouteConfig
) {

  val remotePathSegments = Url(remotePath).segments
  fun matchPath(path: String): Boolean {
    val segments = Url(path).segments
    return if (segments.size != remotePathSegments.size) {
      false
    } else {
      segments.foldIndexed(true) { index, acc, segment ->
        val remoteSegment = remotePathSegments[index]
        val doesMatch = if (remoteSegment.contains("{")) {
          true
        } else {
          remoteSegment == segment
        }
        acc && doesMatch
      }
    }
  }

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
