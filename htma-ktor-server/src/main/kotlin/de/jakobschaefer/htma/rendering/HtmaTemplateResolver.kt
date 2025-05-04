package de.jakobschaefer.htma.rendering

import de.jakobschaefer.htma.loadAppResource
import de.jakobschaefer.htma.webinf.AppManifest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

interface HtmaTemplateResolver {
  fun getTemplate(templateName: String): HtmaTemplate
}

class DevelopmentTemplateResolver : HtmaTemplateResolver {
  override fun getTemplate(templateName: String): HtmaTemplate {
    val templatePath = getTemplatePath(templateName)
    val isLayout = templateName.replace("/", ".")
      .split(".")
      .last()
      .startsWith('_')
    val document = if (isLayout && !templatePath.exists()) {
      Jsoup.parse(DEFAULT_LAYOUT_TEMPLATE_CONTENT)
    } else {
      Jsoup.parse(templatePath)
    }
    return HtmaTemplate(document)
  }

  private fun getTemplatePath(templateName: String): Path {
    return Paths.get("web", "$templateName.html")
  }
}

class ProductionTemplateResolver(
  appManifest: AppManifest,
  private val resourceBase: String,
) : HtmaTemplateResolver {
  private val cache = mutableMapOf<String, HtmaTemplate>()
  override fun getTemplate(templateName: String): HtmaTemplate {
    return cache[templateName]!!
  }

  init {
    loadTemplate("__root")
    for (page in appManifest.pages) {
      page.outletChainList.forEach { outletTemplateName ->
        if (!cache.containsKey(outletTemplateName)) {
          loadTemplate(outletTemplateName)
        }
      }
    }
    for (component in appManifest.components) {
      val templateName = "__components/${component.name}"
      loadTemplate(templateName)
    }
  }

  private fun getTemplateResourcePath(templateName: String): String {
    return "$resourceBase/web/$templateName.html"
  }

  private fun loadTemplateDocument(templatePath: String): Document {
    val templateContent = loadAppResource(templatePath).readAllBytes().toString(Charsets.UTF_8)
    return Jsoup.parse(templateContent)
  }

  private fun loadTemplate(templateName: String) {
    val isLayout = templateName.replace("/", ".")
      .split(".")
      .last()
      .startsWith('_')
    val templatePath = getTemplateResourcePath(templateName)
    val document = if (isLayout) {
      try {
        loadTemplateDocument(templatePath)
      } catch (e: Exception) {
        log.info("Template {} not found. Fallback content will be used.", templateName)
        Jsoup.parse(DEFAULT_LAYOUT_TEMPLATE_CONTENT)
      }
    } else {
      loadTemplateDocument(templatePath)
    }
    log.info("Loading resource {}", templatePath)
    cache[templateName] = HtmaTemplate(document)
  }

  companion object {
    val log = LoggerFactory.getLogger(ProductionTemplateResolver::class.java)!!
  }
}
