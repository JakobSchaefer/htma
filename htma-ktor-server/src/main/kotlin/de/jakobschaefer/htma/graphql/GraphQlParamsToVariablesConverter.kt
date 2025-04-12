package de.jakobschaefer.htma.graphql

import de.jakobschaefer.htma.routing.HtmaParams
import kotlin.math.sin

object GraphQlParamsToVariablesConverter {
  fun convert(params: HtmaParams): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    for ((key, value) in params.filter { it.key.startsWith('$') }) {
      if (key.endsWith("[]")) {
        val segments = key.substring(1).dropLast(2).split('.')
        insertValueIntoStructure(result, segments, value)
        // List Value
      } else {
        val segments = key.substring(1).split('.')
        if (value.isNotEmpty()) {
          val single = value.first()
          insertValueIntoStructure(result, segments, single)
        } else {
          insertValueIntoStructure(result, segments, null)
        }
      }
    }
    return result
  }

  private val indexMatcher = Regex("""(\w+)(\[(\d+)])?""")

  private fun parseKey(key: String): Pair<String, Int?> {
    val match = indexMatcher.matchEntire(key)
    val name = match?.groups?.get(1)?.value ?: key
    val index = match?.groups?.get(3)?.value?.toIntOrNull()
    return name to index
  }

  private fun insertValueIntoStructure(
    root: MutableMap<String, Any?>,
    keys: List<String>,
    value: Any?
  ): MutableMap<String, Any?> {
    var current: MutableMap<String, Any?> = root

    keys.dropLast(1).forEach { key ->
      val (name, index) = parseKey(key)

      if (index == null) {
        val next = current.getOrPut(name) { mutableMapOf<String, Any?>() } as MutableMap<String, Any?>
        current = next
      } else {
        val list = current.getOrPut(name) { mutableListOf<Any?>() } as MutableList<Any?>
        while (list.size <= index) {
          list.add(null)
        }
        var next = list[index] as? MutableMap<String, Any?>
        if (next == null) {
          next = mutableMapOf()
          list[index] = next
        }
        current = next
      }
    }

    // Last entry - now we can set the value
    val (lastName, lastIndex) = parseKey(keys.last())
    if (lastIndex == null) {
      current[lastName] = value
    } else {
      val list = current.getOrPut(lastName) { mutableListOf<Any?>() } as MutableList<Any?>
      while (list.size <= lastIndex) {
        list.add(null)
      }
      list[lastIndex] = value
    }

    return root
  }
}
