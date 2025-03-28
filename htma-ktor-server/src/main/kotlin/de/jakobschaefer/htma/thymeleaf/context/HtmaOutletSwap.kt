package de.jakobschaefer.htma.thymeleaf.context

import de.jakobschaefer.htma.webinf.AppManifestPage

data class HtmaOutletSwap(
  val innerMostCommonOutlet: String,
  val oldOutlet: String,
  val newOutlet: String
) {
  companion object {
    fun build(fromPage: AppManifestPage, toPage: AppManifestPage): HtmaOutletSwap {
      var innerMostCommonOutlet = "__root"
      while (
        fromPage.outlets[innerMostCommonOutlet] != null
          && fromPage.outlets[innerMostCommonOutlet] == toPage.outlets[innerMostCommonOutlet]
      ) {
        innerMostCommonOutlet = fromPage.outlets[innerMostCommonOutlet]!!
      }
      val oldOutlet = fromPage.outlets[innerMostCommonOutlet] ?: innerMostCommonOutlet
      val newOutlet = toPage.outlets[innerMostCommonOutlet] ?: innerMostCommonOutlet

      return HtmaOutletSwap(
        innerMostCommonOutlet = innerMostCommonOutlet,
        oldOutlet = oldOutlet,
        newOutlet = newOutlet
      )
    }

  }
}
