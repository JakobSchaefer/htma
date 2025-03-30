package de.jakobschaefer.htma.routing

import de.jakobschaefer.htma.Logs
import de.jakobschaefer.htma.htma
import de.jakobschaefer.htma.replyHtml
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@KtorDsl
fun Route.web(spec: HtmaWebSpecBuilder.() -> Unit) {
  val appManifest = application.htma.appManifest
  val resourceBase = application.htma.resourceBase

  val webSpec = HtmaWebSpecBuilder().apply(spec).build()

  staticResources("/assets", "$resourceBase/assets") {
    cacheControl { listOf(CacheControl.MaxAge(maxAgeSeconds = 31_104_000)) }
    preCompressed(CompressedFileType.GZIP)
  }

  for (page in appManifest.pages) {
    val canonicalPathSegments = page.canonicalPath.split(".")
    val pageDataLoadersByName = mutableMapOf<String, HtmaDataLoader>()
    for (dataLoader in webSpec.dataLoaders) {
      if (canonicalPathSegments.startsWith(dataLoader.canonicalPathSegments)) {
        val existingDataLoader = pageDataLoadersByName[dataLoader.name]
        if (existingDataLoader == null) {
          pageDataLoadersByName[dataLoader.name] = dataLoader
        } else {
          throw IllegalStateException("Conflicting data loaders ${existingDataLoader.canonicalPath} and ${dataLoader.canonicalPath} for data.${dataLoader.name}")
        }
      }
    }
    val pageDataLoaders = pageDataLoadersByName.values
    Logs.htma.info("GET {} leads to page {} with data loaders {}", page.remotePath, page.filePath, pageDataLoaders.map { it.name })
    get(page.remotePath) {
      val data = coroutineScope {
        pageDataLoaders.map { dataLoader ->
          async {
            Pair(dataLoader.name, dataLoader.load(this@get))
          }
        }.awaitAll()
          .toMap()
      }
      call.replyHtml(page, data)
    }
  }
}

fun <T> List<T>.startsWith(sublist: List<T>): Boolean {
  return this.take(sublist.size) == sublist
}
