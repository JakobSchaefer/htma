package de.jakobschaefer.htma

import io.ktor.client.statement.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.test.appender.ListAppender
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import java.util.UUID

fun captureLogs(logger: Logger): ListAppender {
  val randomUuid = UUID.randomUUID().toString()
  val listAppender = ListAppender("test-appender-$randomUuid").also { it.start() }
  val log4jLogger = LoggerContext.getContext(false).getLogger(logger.name)
  log4jLogger.addAppender(listAppender)
  return listAppender
}

@KtorDsl
fun withTestServer(spec: suspend ApplicationTestBuilder.() -> Unit) {
  testApplication {
    serverConfig {
      developmentMode = false
    }
    spec()
  }
}

suspend fun HttpResponse.bodyAsHtml(): Document {
  val body = bodyAsText()
  return Jsoup.parse(body)
}
