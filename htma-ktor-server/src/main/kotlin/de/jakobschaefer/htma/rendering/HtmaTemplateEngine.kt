package de.jakobschaefer.htma.rendering

import de.jakobschaefer.htma.messages.HtmaMessageFormatter
import de.jakobschaefer.htma.rendering.thymeleaf.HtmaDialect
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver

internal class HtmaTemplateEngine(
  isDevelopmentMode: Boolean,
  resourceBase: String,
  appManifest: AppManifest,
  viteManifest: ViteManifest,
  messageFormatter: HtmaMessageFormatter
) {
  private val templateEngine = TemplateEngine()

  init {
    templateEngine.clearDialects()
    templateEngine.addDialect(HtmaDialect(messageFormatter, appManifest))
    templateEngine.linkBuilders = setOf(HtmaLinkBuilder())
    templateEngine.messageResolvers = setOf()

    val htmlTemplateResolver = getHtmlTemplateResolver(isDevelopmentMode, resourceBase)
    templateEngine.addTemplateResolver(htmlTemplateResolver)
  }

  fun getHtmlTemplateResolver(isDevelopmentMode: Boolean, resourceBase: String): ITemplateResolver {
    return if (isDevelopmentMode) {
      FileTemplateResolver().apply {
        prefix = "web/"
        suffix = ".html"
        templateMode = TemplateMode.HTML
        isCacheable = false
        characterEncoding = "UTF-8"
      }
    } else {
      ClassLoaderTemplateResolver().apply {
        prefix = "$resourceBase/web/"
        suffix = ".html"
        templateMode = TemplateMode.HTML
        isCacheable = true
        characterEncoding = "UTF-8"
      }
    }
  }

  fun process(context: HtmaContext): String {
    return templateEngine.process("__root", context)
  }
}

