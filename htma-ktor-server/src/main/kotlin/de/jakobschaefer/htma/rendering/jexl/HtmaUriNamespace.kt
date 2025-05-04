package de.jakobschaefer.htma.rendering.jexl

import com.damnhandy.uri.template.UriTemplate
import io.ktor.http.Url
import java.nio.file.Paths
import kotlin.io.path.pathString

internal class HtmaUriNamespace(
  val context: HtmaContext
) {
  fun template(uriTemplate: String): String {
    return template(uriTemplate, context.params)
  }

  /**
   * Use this function to expand URI templates.
   *
   * RFC 6570 defines various expression types that can be used within URL templates, including:
   *
   * - **Simple String Expansion**: Represented as `{varname}` and are replaced by their corresponding values.
   * - **Reserved Expansion**: Represented as `{+varname}` and are used for expansion without percent encoding. Useful for query strings.
   * - **Fragment Expansion**: Represented as `{#varname}` and used for expansion within a URL fragment.
   * - **Label Expansion with Dot-Prefix**: Represented as `{.varname}` and used for dot notation in path segments.
   * - **Path Segment Expansion**: Represented as `{/varname}` and used for inserting values into path segments.
   * - **Query Expansion**: Represented as `{?varname}` and used for adding query parameters.
   * - **Continuation**: Represented as `{&varname}` and used to continue a query with additional parameters.
   *
   * **Example**:
   * ```html
   * <!-- Input -->
   * <a data-x-href="uri:template('https://example.com{?name}', { 'name': 'John Doe' })">Link</a>
   * <!-- Output -->
   * <a href="https://example.com?name=John%20Doe">Link</a>
   * ```
   *
   * @param uriTemplate URI Template e.g.
   * @return The expanded URI
   * @see [RFC6570](https://www.rfc-editor.org/rfc/rfc6570)
   */
  fun template(uriTemplate: String, params: Map<String, Any>): String {
    val tpl = UriTemplate.fromTemplate(uriTemplate)
    tpl
    return UriTemplate.fromTemplate(uriTemplate).expand(params)
  }

  fun match(matchStr: String, location: String): Boolean {
    val matchSegments = Url(matchStr).segments
    val locationSegments = Url(location).segments
    return if (matchSegments.size == locationSegments.size) {
      matchSegments.foldIndexed(true) { index, acc, matchSegment ->
        val match = matchSegment.startsWith("$") || matchSegment == locationSegments[index]
        acc && match
      }
    } else {
      false
    }
  }

  fun asset(relPath: String): String {
    val relativeAssetPath = Paths.get(relPath)
    val htmlFilePath = Paths.get("web/${context.htmaState.toPage.webPath}")
    val webAssetPath = htmlFilePath.parent
      .resolve(relativeAssetPath)
      .normalize()
      .pathString
      .replace("\\", "/") // windows

    return if (context.htmaState.isDevelopmentMode) {
      "http://localhost:5174/$webAssetPath"
    } else {
      val contextPath = "/"
      contextPath + context.htmaState.viteManifest.assets[webAssetPath]!!.file
    }
  }
}
