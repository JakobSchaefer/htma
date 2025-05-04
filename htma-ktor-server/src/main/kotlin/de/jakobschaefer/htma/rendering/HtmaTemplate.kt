package de.jakobschaefer.htma.rendering

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class HtmaTemplate(
  val document: Document
) {
  val plug: Element? = document.body().firstElementChild()
}
