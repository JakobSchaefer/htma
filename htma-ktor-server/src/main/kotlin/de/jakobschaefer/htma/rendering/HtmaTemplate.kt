package de.jakobschaefer.htma.rendering

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

const val DEFAULT_LAYOUT_TEMPLATE_CONTENT = """
  <div>
    <outlet></outlet>
  </div>
"""

class HtmaTemplate(
  val document: Document
) {
  val plug: Element? = document.body().firstElementChild()
}
