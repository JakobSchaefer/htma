package de.jakobschaefer.htma.webinf

import de.jakobschaefer.htma.JSON
import de.jakobschaefer.htma.loadAppResource
import de.jakobschaefer.htma.rendering.htma
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream

@Serializable
data class AppManifest(
  val pages: List<AppManifestPage>,
  val webComponents: List<AppManifestWebComponent>,
  val graphQlDocuments: Map<String, AppManifestGraphQlDocument>
) {

  fun findPageByPath(path: String): AppManifestPage {
    return pages.maxBy {
      if (it.matchPath(path)) {
        it.remotePathPriority
      } else {
        Int.MIN_VALUE
      }
    }
  }

  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromResources(resourceBase: String): AppManifest {
      val manifest = loadAppResource("${resourceBase}/manifest.json")
      val appManifest = JSON.decodeFromStream<AppManifest>(manifest)
      return appManifest
    }
  }
}

