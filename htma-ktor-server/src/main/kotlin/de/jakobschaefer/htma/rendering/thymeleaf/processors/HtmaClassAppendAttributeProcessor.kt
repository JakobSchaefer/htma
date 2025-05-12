package de.jakobschaefer.htma.rendering.thymeleaf.processors

import de.jakobschaefer.htma.rendering.thymeleaf.evaluateJexl
import org.thymeleaf.context.ITemplateContext
import org.thymeleaf.engine.AttributeName
import org.thymeleaf.model.IProcessableElementTag
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor
import org.thymeleaf.processor.element.IElementTagStructureHandler
import org.thymeleaf.templatemode.TemplateMode

class HtmaClassAppendAttributeProcessor(
  dialectPrefix: String,
  precedence: Int
) : AbstractAttributeTagProcessor(
  TemplateMode.HTML,
  dialectPrefix,
  null,
  false,
  "class-append",
  true,
  precedence,
  true
) {

  override fun doProcess(
    context: ITemplateContext,
    tag: IProcessableElementTag,
    attributeName: AttributeName,
    attributeValue: String,
    structureHandler: IElementTagStructureHandler
  ) {
    val classes = tag.getAttributeValue("class") ?: ""
    val newClasses = context.evaluateJexl(attributeValue)
    val resultClasses = when (newClasses) {
      is String -> joinClasses(classes, newClasses)
      is List<*> -> joinClasses(classes, newClasses.filterIsInstance<String>())
      is Map<*, *> -> joinClasses(classes, newClasses)
      else -> throw IllegalArgumentException("Attribute ${attributeName.attributeName} has to be of type String, List<String> or Map<String, Boolean>")
    }
    structureHandler.setAttribute("class", resultClasses)
  }

  fun joinClasses(classes: String, newClasses: String): String {
    return if (classes.isBlank()) newClasses else "$classes $newClasses"
  }

  fun joinClasses(classes: String, classMap: Map<*, *>): String {
    val newClasses = classMap.mapNotNull { (key, value) ->
      when (value) {
        is Boolean -> if (value) "$key " else null
        else -> throw IllegalArgumentException("Attribute class-append has to be of type Map<String, Boolean>")
      }
    }
    return if (newClasses.isEmpty()) {
      return classes
    } else {
      return joinClasses(classes, newClasses.joinToString(" "))
    }
  }

  fun joinClasses(classes: String, newClasses: List<String>): String {
    return joinClasses(classes, newClasses.joinToString(" "))
  }
}
