package de.jakobschaefer.htma

import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.linkbuilder.StandardLinkBuilder

/**
 * Link builder responsible to resolve expressions like @{...}
 * We basically want to use thymeleaf's standard dialect, but we need to lookup hashed vite assets
 * See: https://www.thymeleaf.org/doc/articles/standardurlsyntax.html
 */
class HtmaLinkBuilder : StandardLinkBuilder() {
  override fun computeContextPath(
    context: IExpressionContext,
    base: String,
    parameters: MutableMap<String, Any>?
  ): String {
    return "/"
  }

  override fun processLink(context: IExpressionContext, link: String): String {
    val htma = HtmaRoutingCall.fromContext(context)
    if (link.matches(Regex("^https?://.+"))) {
      return link
    }

    if (link.startsWith("~/")) {
      return link.substring(1)
    }

    if (link.startsWith("/")) {
      return link
    }

    // Assets must be relative.
    return if (htma.isDevelopment) {
      // serve via vite
      "http://localhost:5173/web/$link"
    } else {
      val assetFile = htma.vite.assets["web/$link"]
      if (assetFile != null) {
        "/${assetFile.file}"
      } else {
        // We really don't know.... keep the link as is
        "/$link"
      }
    }
  }
}
