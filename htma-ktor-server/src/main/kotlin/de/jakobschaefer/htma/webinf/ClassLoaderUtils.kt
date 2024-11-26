package de.jakobschaefer.htma.webinf

import java.io.InputStream

fun loadResource(path: String): InputStream {
  return if (path.startsWith("/")) {
    object {}.javaClass.getResourceAsStream(path)!!
  } else {
    object {}.javaClass.getResourceAsStream("/$path")!!
  }
}
