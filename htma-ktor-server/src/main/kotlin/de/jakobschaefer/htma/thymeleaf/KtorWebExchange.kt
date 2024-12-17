package de.jakobschaefer.htma.thymeleaf

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.thymeleaf.web.IWebApplication
import org.thymeleaf.web.IWebExchange
import org.thymeleaf.web.IWebRequest
import org.thymeleaf.web.IWebSession
import java.security.Principal
import java.util.*

class KtorWebExchange(
  val call: RoutingCall
) : IWebExchange {
  override fun getRequest(): IWebRequest {
    return KtorWebRequest(call.request)
  }

  override fun getSession(): IWebSession {
    TODO("Not yet implemented")
  }

  override fun getApplication(): IWebApplication {
    TODO("Not yet implemented")
  }

  override fun getPrincipal(): Principal {
    TODO("Not yet implemented")
  }

  override fun getLocale(): Locale {
    TODO("Not yet implemented")
  }

  override fun getContentType(): String {
    TODO("Not yet implemented")
  }

  override fun getCharacterEncoding(): String {
    TODO("Not yet implemented")
  }

  override fun containsAttribute(name: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun getAttributeCount(): Int {
    TODO("Not yet implemented")
  }

  override fun getAllAttributeNames(): MutableSet<String> {
    TODO("Not yet implemented")
  }

  override fun getAttributeMap(): Map<String, Any?> {
    return attrs
  }

  override fun getAttributeValue(name: String): Any? {
    return attrs[name]
  }

  override fun setAttributeValue(name: String, value: Any?) {
    if (value != null) {
      attrs[name] = value
    } else {
      attrs.remove(name)
    }
  }

  override fun removeAttribute(name: String) {
    TODO("Not yet implemented")
  }

  override fun transformURL(url: String): String {
    TODO("Not yet implemented")
  }

  private val attrs = HashMap<String, Any>()
}
