package de.jakobschaefer.htma.thymeleaf

import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.AppManifestPage
import de.jakobschaefer.htma.webinf.vite.ViteManifest

data class HtmaRenderContext(
  val isDevelopment: Boolean,
  val vite: ViteManifest,
  val app: AppManifest,
  val fromPage: AppManifestPage?,
  val toPage: AppManifestPage,
  val isHtmxRequest: Boolean,
  val outletSwap: HtmaOutletSwap?
)
