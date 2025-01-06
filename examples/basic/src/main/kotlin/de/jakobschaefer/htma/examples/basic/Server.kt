package de.jakobschaefer.htma.examples.basic

import de.jakobschaefer.htma.Htma
import de.jakobschaefer.htma.routing.htma
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.util.*

fun Application.module() {
  install(Htma) {
    supportedLocales = listOf(Locale.ENGLISH, Locale.GERMAN)
    fallbackLocale = Locale.ENGLISH
  }

  routing {
    htma {
    }
  }
}
