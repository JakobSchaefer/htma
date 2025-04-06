package de.jakobschaefer.htma

import java.io.InputStream

fun loadAppResource(path: String): InputStream {
  return if (path.startsWith("/")) {
    object {}.javaClass.getResourceAsStream(path)!!
  } else {
    object {}.javaClass.getResourceAsStream("/$path")!!
  }
}
