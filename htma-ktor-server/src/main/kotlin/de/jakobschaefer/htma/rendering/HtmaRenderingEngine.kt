package de.jakobschaefer.htma.rendering

import com.google.gson.GsonBuilder
import de.jakobschaefer.htma.messages.HtmaFormatter
import de.jakobschaefer.htma.rendering.jexl.HtmaContext
import de.jakobschaefer.htma.rendering.jexl.HtmaFormattedMessage
import de.jakobschaefer.htma.rendering.jexl.HtmaJexl
import de.jakobschaefer.htma.rendering.jexl.HtmaUrlNamespace
import de.jakobschaefer.htma.webinf.AppManifest
import io.ktor.util.*
import org.apache.commons.jexl3.JexlContext
import org.jsoup.nodes.Attribute
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

const val ATTRIBUTE_PREFIX = "data-x-"

internal class HtmaRenderingEngine(
  isDevelopmentMode: Boolean,
  resourceBase: String,
  val appManifest: AppManifest,
  val formatter: HtmaFormatter,
  val defaultLocale: Locale
) {
  private val gson = GsonBuilder()
    .setPrettyPrinting()
    .serializeNulls()
    .create()
  private val templateResolver: HtmaTemplateResolver = if (isDevelopmentMode) {
    DevelopmentTemplateResolver()
  } else {
    ProductionTemplateResolver(appManifest, resourceBase)
  }
  private val jexl = HtmaJexl.build()

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
    process(rootDoc, htmaContext)
    expandComponents(rootDoc, htmaContext)
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

  private fun process(element: Element, context: HtmaContext): Element {
    // Expand each attributes
    while (true) {
      val eachAttributeName = "${ATTRIBUTE_PREFIX}each"
      val eachTag = element.selectFirst("[$eachAttributeName]") ?: break
      val eachAttribute = eachTag.attribute(eachAttributeName)!!
      val eachAttributeValue = evaluateJexlAttributeValue(eachAttribute, context)
      when (eachAttributeValue) {
        is Collection<*> -> {
          for (value in eachAttributeValue) {
            val eachTagCloned = eachTag.clone()
            eachTagCloned.removeAttr(eachAttributeName)
            context.pushIt(value)
            val processAndClonedEachTag = process(eachTagCloned, context)
            context.popIt()
            eachTag.before(processAndClonedEachTag)
          }
        }
      }
      eachTag.remove()
    }

    // resolve image asset urls
    val images = element.select("img[src^=.]")
    val urlNamespace = HtmaUrlNamespace(context)
    for (img in images) {
      img.attr("src", urlNamespace.asset(img.attr("src")))
    }

    // boost anchors
    val anchors = element.getElementsByTag("a")
    for (anchor in anchors) {
      val hrefValue = anchor.attr("href")
      anchor.attr("hx-get", hrefValue)
    }

    // boost forms
    val forms = element.select("form")
    for (form in forms) {
      form.attr("enctype", "multipart/form-data")
      val actionValue = form.attr("action")
      val methodValue = if (form.hasAttr("method")) {
        form.attr("method")
      } else {
        "get"
      }
      if (methodValue == "get") {
        form.attr("hx-get", actionValue)
      } else {
        form.attr("hx-post", actionValue)
      }
    }

    processAttributes(element, context)
    return element
  }

  private fun evaluateJexlAttributeValue(attribute: Attribute, context: JexlContext): Any? {
    val attributeValue = attribute.value
    val attributeExpression = jexl.createExpression(attributeValue)
    return attributeExpression.evaluate(context)
  }

  private fun processAttributes(root: Element, context: JexlContext) {
    val expressionTags = root.getElementsByAttributeStarting(ATTRIBUTE_PREFIX)
    val operations = mutableListOf<ElementOperation>()
    for (tag in expressionTags) {
      for (attribute in tag.attributes()) {
        if (attribute.key.startsWith(ATTRIBUTE_PREFIX)) {
          val attributeKey = attribute.key.substringAfter(ATTRIBUTE_PREFIX)
          operations.add(ElementOperation.DeleteAttribute(tag, attribute.key))
          val attributeResult = evaluateJexlAttributeValue(attribute, context)
          when (attributeKey) {
            "html" -> {
              when (attributeResult) {
                is String -> operations.add(ElementOperation.WriteInnerHtml(tag, attributeResult))
                else -> operations.add(ElementOperation.WriteInnerHtml(tag, gson.toJson(attributeResult)))
              }
            }
            "text" -> {
              when (attributeResult) {
                is String -> operations.add(ElementOperation.WriteInnerText(tag, attributeResult))
                is HtmaFormattedMessage -> if (attributeResult.message != null) {
                  operations.add(ElementOperation.WriteInnerText(tag, attributeResult.message))
                } else {
                  val innerHtml = tag.html()
                  val formattedInnerHtml = formatter.format(defaultLocale, innerHtml, attributeResult.params)
                  operations.add(ElementOperation.WriteInnerText(tag, formattedInnerHtml))
                }
                else -> {
                  operations.add(
                    ElementOperation.WriteInnerText(tag, gson.toJson(attributeResult)))
                }
              }
            }
            "mutation" -> {
              when (attributeResult) {
                is String -> {
                  val operationNameInput = Element("input")
                  operationNameInput.attr("type", "hidden")
                  operationNameInput.attr("name", "__operationName")
                  operationNameInput.attr("value", attributeResult)
                  operations.add(ElementOperation.AddChildElement(tag, operationNameInput))
                }
                else -> throw IllegalArgumentException("Operation attribute must be a string")
              }
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

  private fun expandComponents(rootDoc: Document, context: HtmaContext) {
    for (availableComponent in appManifest.components) {
      val foundComponentsInDocument = rootDoc.getElementsByTag(availableComponent.name)
      if (foundComponentsInDocument.size > 0) {
        val template = templateResolver.getTemplate("__components/${availableComponent.name}").document
          .clone()
          .selectFirst("template")!!
        for (component in foundComponentsInDocument) {
          val processedTemplate = template.clone()
          val attributes = component.attributes().associate { it.key to it.value }
          context.set("attributes", attributes)
          process(processedTemplate, context)
          context.set("attributes", null)
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
  class WriteInnerHtml(val tag: Element, val innerHtml: String): ElementOperation {
    override fun execute() {
      tag.html(innerHtml)
    }
  }
  class WriteInnerText(val tag: Element, val text: String): ElementOperation {
    override fun execute() {
      tag.text(text)
    }
  }
  class AddChildElement(val tag: Element, val childElement: Element): ElementOperation {
    override fun execute() {
      tag.appendChild(childElement)
    }
  }
}
