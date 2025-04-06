package de.jakobschaefer.htma.rendering

import com.google.gson.GsonBuilder
import de.jakobschaefer.htma.webinf.AppManifest
import io.ktor.util.*
import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.JexlContext
import org.apache.commons.jexl3.JexlFeatures
import org.apache.commons.jexl3.MapContext
import org.apache.commons.jexl3.introspection.JexlPermissions
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

const val ATTRIBUTE_PREFIX = "x-"

internal class HtmaRenderingEngine(
  isDevelopmentMode: Boolean,
  resourceBase: String,
  val appManifest: AppManifest,
) {
  private val gson = GsonBuilder()
    .setPrettyPrinting()
    .serializeNulls()
    .create()
  private val templateResolver: HtmaTemplateResolver
  private val jexl = JexlBuilder()
    .permissions(JexlPermissions.UNRESTRICTED)
    .features(JexlFeatures()
      .script(false)
      .loops(false)
      .sideEffect(false)
      .sideEffectGlobal(false)
    )
    .namespaces(
      mapOf(
        "uri" to HtmaContextUrlNamespace()
      )
    )
    .create()

  init {
    templateResolver = if (isDevelopmentMode) {
      DevelopmentTemplateResolver()
    } else {
      ProductionTemplateResolver(appManifest, resourceBase)
    }
  }

  fun renderFragment(htmaState: HtmaState, htmaContext: HtmaContext): String {
    return renderPage(htmaState, htmaContext, entrypoint = htmaState.outletSwap!!.newOutlet)
  }

  fun renderPage(htmaState: HtmaState, htmaContext: HtmaContext, entrypoint: String = "__root"): String {
    val rootDoc = templateResolver.getTemplate(entrypoint).document.clone()
    rootDoc.body().firstElementChild()!!.attr("id", entrypoint)
    var currentOutletName = entrypoint
    var currentOutlet = rootDoc.selectFirst("outlet")
    while (currentOutlet != null) {
      val plugName = htmaState.toPage.outletChain[currentOutletName]
      if (plugName == null) {
        currentOutlet.remove()
        currentOutlet = null
      } else {
        val plug = templateResolver.getTemplate(plugName).plug?.clone()
        if (plug == null) {
          currentOutlet.remove()
          currentOutlet = null
        } else {
          plug.attr("id", plugName)
          currentOutlet.replaceWith(plug)
          currentOutletName = plugName
          currentOutlet = plug.selectFirst("outlet")
        }
      }
    }
    if (entrypoint == "__root") {
      addScriptsAndStylesToRoot(rootDoc, htmaState)
    }
    processExpressions(rootDoc, htmaContext)
    expandComponents(rootDoc)
    return rootDoc.html()
  }

  private fun addScriptsAndStylesToRoot(rootDoc: Document, htmaState: HtmaState) {
    for (js in htmaState.viteManifest.mainJsModules) {
      val script = rootDoc.head().appendElement("script")
      script.attr("type", "module")
      script.attr("src", js)
    }
    for (css in htmaState.viteManifest.mainCssModules) {
      val link = rootDoc.head().appendElement("link")
      link.attr("rel", "stylesheet")
      link.attr("href", css)
    }
  }

  private fun processExpressions(rootDoc: Element, context: JexlContext) {
    val expressionTags = rootDoc.getElementsByAttributeStarting(ATTRIBUTE_PREFIX)
    val operations = mutableListOf<ElementOperation>()
    for (tag in expressionTags) {
      for (attribute in tag.attributes()) {
        if (attribute.key.startsWith(ATTRIBUTE_PREFIX)) {
          val attributeKey = attribute.key.substringAfter(ATTRIBUTE_PREFIX)
          val attributeValue = attribute.value
          operations.add(ElementOperation.DeleteAttribute(tag, attribute.key))
          val attributeExpression = jexl.createExpression(attributeValue)
          val attributeResult = attributeExpression.evaluate(context)
          when (attributeKey) {
            "utext" -> {
              when (attributeResult) {
                is String -> operations.add(ElementOperation.WriteInnerHtml(tag, attributeResult))
                else -> operations.add(ElementOperation.WriteInnerHtml(tag, gson.toJson(attributeResult)))
              }
            }
            "text" -> {
              when (attributeResult) {
                is String -> operations.add(ElementOperation.WriteInnerHtml(tag, attributeResult.escapeHTML()))
                else -> operations.add(ElementOperation.WriteInnerHtml(tag, gson.toJson(attributeResult).escapeHTML()))
              }
            }
            "navigate" -> {
              val href = attributeResult as String
              operations.add(ElementOperation.WriteStringAttribute(tag, "href", href))
              operations.add(ElementOperation.WriteStringAttribute(tag, "hx-get", href))
              operations.add(ElementOperation.WriteStringAttribute(tag, "hx-push-url", "true"))
            }
            else -> when (attributeResult) {
              is String -> operations.add(ElementOperation.WriteStringAttribute(tag, attributeKey, attributeResult))
              is Boolean -> operations.add(ElementOperation.WriteBooleanAttribute(tag, attributeKey, attributeResult))
              else -> operations.add(ElementOperation.WriteStringAttribute(tag, attributeKey, gson.toJson(attributeResult)))
            }
          }
        }
      }
    }

    for (operation in operations) {
      operation.execute()
    }
  }

  private fun expandComponents(rootDoc: Document) {
    for (availableComponent in appManifest.components) {
      val foundComponentsInDocument = rootDoc.getElementsByTag(availableComponent.name)
      if (foundComponentsInDocument.size > 0) {
        val template = templateResolver.getTemplate("__components/${availableComponent.name}").document
          .clone()
          .selectFirst("template")!!
        for (component in foundComponentsInDocument) {
          val processedTemplate = template.clone()
          val attributes = component.attributes().associate { it.key to it.value }
          val ctx = MapContext()
          ctx.set("attributes", attributes)
          processExpressions(processedTemplate, ctx)
          component.appendChild(processedTemplate)
        }
      }
    }
  }
}

sealed interface ElementOperation {

  fun execute()

  class DeleteAttribute(val tag: Element, val attributeKey: String) : ElementOperation {
    override fun execute() {
      tag.removeAttr(attributeKey)
    }
  }
  class WriteStringAttribute(val tag: Element, val attributeKey: String, val attributeValue: String) : ElementOperation {
    override fun execute() {
      tag.attr(attributeKey, attributeValue)
    }
  }
  class WriteBooleanAttribute(val tag: Element, val attributeKey: String, val attributeValue: Boolean) : ElementOperation {
    override fun execute() {
      tag.attr(attributeKey, attributeValue)
    }
  }
  class WriteInnerHtml(val tag: Element, val text: String): ElementOperation {
    override fun execute() {
      tag.html(text)
    }
  }
}
