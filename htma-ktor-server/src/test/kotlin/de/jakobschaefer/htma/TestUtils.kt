package de.jakobschaefer.htma

import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.test.appender.ListAppender
import org.slf4j.Logger
import java.util.UUID

fun captureLogs(logger: Logger): ListAppender {
  val randomUuid = UUID.randomUUID().toString()
  val listAppender = ListAppender("test-appender-$randomUuid").also { it.start() }
  val log4jLogger = LoggerContext.getContext(false).getLogger(logger.name)
  log4jLogger.addAppender(listAppender)
  return listAppender
}
