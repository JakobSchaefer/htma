package de.jakobschaefer.htma.rendering

import de.jakobschaefer.htma.messages.HtmaMessageFormatter
import de.jakobschaefer.htma.rendering.state.HtmaState
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jsoup.Jsoup
import java.util.Locale

class HtmaTemplateEngineTests : FunSpec({
  test("outlets are resolved correctly") {
    val resourceBase = "WEB-INF"
    val appManifest = AppManifest.loadFromResources(resourceBase)
    val viteManifest = ViteManifest.loadFromResources(resourceBase)
    val engine = HtmaTemplateEngine(
      isDevelopmentMode = false,
      resourceBase = resourceBase,
      appManifest = appManifest,
      viteManifest = viteManifest,
      messageFormatter = HtmaMessageFormatter()
    )

    val context = HtmaContext(
      locale = Locale.of("de"),
      location = "/blog/new",
      params = emptyMap(),
      htma = HtmaState(
        isDevelopmentMode = false,
        resourceBase = resourceBase,
        appManifest = appManifest,
        viteManifest = viteManifest,
        defaultLocale = Locale.of("de"),
        supportedLocales = listOf(Locale.of("de")),
        toPage = appManifest.pages.find { it.templateName == "blog/new" }!!,
        isLogicEnabled = false,
        isFetchRequest = false,
        fromPage = null,
      ),
      query = emptyMap(),
      mutation = emptyMap(),
    )

    val response = engine.process(context)
    val htmlReply = Jsoup.parse(response)
    htmlReply.getElementById("blog").shouldNotBeNull()
    htmlReply.getElementById("blog/_layout").shouldNotBeNull()
    htmlReply.getElementById("blog/_layout.new").shouldNotBeNull()
  }

  test("web components are rendered with shadow dom template") {
    val resourceBase = "WEB-INF-web-components"
    val appManifest = AppManifest.loadFromResources(resourceBase)
    val viteManifest = ViteManifest.loadFromResources(resourceBase)
    val engine = HtmaTemplateEngine(
      isDevelopmentMode = false,
      resourceBase = resourceBase,
      appManifest = appManifest,
      viteManifest = viteManifest,
      messageFormatter = HtmaMessageFormatter()
    )

    val context = HtmaContext(
      locale = Locale.of("de"),
      location = "/",
      params = mapOf("count" to listOf("123456")),
      htma = HtmaState(
        isDevelopmentMode = false,
        resourceBase = resourceBase,
        appManifest = appManifest,
        viteManifest = viteManifest,
        defaultLocale = Locale.of("de"),
        supportedLocales = listOf(Locale.of("de")),
        toPage = appManifest.pages.find { it.templateName == "index" }!!,
        isLogicEnabled = false,
        isFetchRequest = false,
        fromPage = null,
      ),
      query = emptyMap(),
      mutation = emptyMap(),
    )

    val response = engine.process(context)
    val htmlReply = Jsoup.parse(response)
    val template = htmlReply.getElementsByTag("example-web-component")
      .select("template")
    template.size shouldBe 1
    template.attr("shadowrootmode") shouldBe "open"

    val count = template.selectFirst("#count")
      .shouldNotBeNull()
    count.text() shouldBe "123456"
  }
})
