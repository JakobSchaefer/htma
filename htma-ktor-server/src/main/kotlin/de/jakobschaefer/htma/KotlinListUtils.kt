package de.jakobschaefer.htma

internal inline fun <T, R> Iterable<T>.lookup(predicate: (T) -> R?): R? {
  for (element in this) {
    val result = predicate(element)
    if (result != null) {
      return result
    }
  }
  return null
}
