package de.jakobschaefer.htma.rendering.state

import de.jakobschaefer.htma.webinf.AppManifestPage
import kotlinx.serialization.Serializable

@Serializable
data class HtmaOutletSwap(
  val innerMostCommonOutlet: String,
  val oldOutlet: String,
  val newOutlet: String
) {
  companion object {
    fun build(fromPage: AppManifestPage, toPage: AppManifestPage): HtmaOutletSwap {
      var innerMostCommonOutlet = "__root"
      while (
        fromPage.outletChain[innerMostCommonOutlet] != null
          && fromPage.outletChain[innerMostCommonOutlet] == toPage.outletChain[innerMostCommonOutlet]
      ) {
        innerMostCommonOutlet = fromPage.outletChain[innerMostCommonOutlet]!!
      }
      val oldOutlet = fromPage.outletChain[innerMostCommonOutlet] ?: innerMostCommonOutlet
      val newOutlet = toPage.outletChain[innerMostCommonOutlet] ?: innerMostCommonOutlet

      return HtmaOutletSwap(
        innerMostCommonOutlet = innerMostCommonOutlet,
        oldOutlet = oldOutlet,
        newOutlet = newOutlet
      )
    }

  }
}
