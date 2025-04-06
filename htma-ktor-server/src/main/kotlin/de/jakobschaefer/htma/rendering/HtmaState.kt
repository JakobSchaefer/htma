package de.jakobschaefer.htma.rendering

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.AppManifestPage
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.util.*

data class HtmaState(
  val isDevelopmentMode: Boolean,
  val resourceBase: String,
  val viteManifest: ViteManifest,
  val appManifest: AppManifest,
  val supportedLocales: List<Locale>,
  val defaultLocale: Locale,
  val isLogicEnabled: Boolean,
  val toPage: AppManifestPage,
  val isFetchRequest: Boolean,
  val fromPage: AppManifestPage?,
  val outletSwap: HtmaStateOutletSwap?,
) {
  companion object {
    internal fun build(call: RoutingCall, toPage: AppManifestPage, configuration: HtmaConfiguration): HtmaState {
      val isFetchRequest = call.request.header("HX-Request") == "true"
      val fromPage = if (isFetchRequest) {
        val fromPathSegments = Url(call.request.header("HX-Current-URL")!!).segments
        detectFromPage(fromPathSegments, configuration.appManifest)
      } else {
        null
      }
      return HtmaState(
        isDevelopmentMode = configuration.isDevelopmentMode,
        resourceBase = configuration.resourceBase,
        viteManifest = configuration.viteManifest,
        appManifest = configuration.appManifest,
        supportedLocales = configuration.supportedLocales,
        defaultLocale = configuration.defaultLocale,
        isLogicEnabled = configuration.isLogicEnabled,
        toPage = toPage,
        isFetchRequest = isFetchRequest,
        fromPage = fromPage,
        outletSwap = fromPage?.let { HtmaStateOutletSwap.build(fromPage, toPage) },
      )
    }

    private fun detectFromPage(pathSegments: List<String>, appManifest: AppManifest): AppManifestPage {
      return appManifest.pages
          .find {
            val remotePathSegments = if (it.remotePath == "/") {
              emptyList()
            } else {
              it.remotePath.substring(1).split("/")
            }
            if (pathSegments.size != remotePathSegments.size) {
              return@find false
            }
            for (i in pathSegments.indices) {
              if (pathSegments[i] != remotePathSegments[i] && !remotePathSegments[i].startsWith('{')) {
                return@find false
              }
            }
            return@find true
          }!!
    }
  }
}
