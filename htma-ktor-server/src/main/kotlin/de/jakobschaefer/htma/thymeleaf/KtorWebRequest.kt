package de.jakobschaefer.htma.thymeleaf

import io.ktor.server.request.*
import org.thymeleaf.web.IWebRequest

class KtorWebRequest(
  val request: ApplicationRequest
) : IWebRequest {
  override fun getMethod(): String {
    TODO("Not yet implemented")
  }

  override fun getScheme(): String {
    TODO("Not yet implemented")
  }

  override fun getServerName(): String {
    TODO("Not yet implemented")
  }

  override fun getServerPort(): Int {
    TODO("Not yet implemented")
  }

  override fun getApplicationPath(): String {
    TODO("Not yet implemented")
  }

  override fun getPathWithinApplication(): String {
    TODO("Not yet implemented")
  }

  override fun getQueryString(): String {
    TODO("Not yet implemented")
  }

  override fun containsHeader(name: String?): Boolean {
    TODO("Not yet implemented")
  }

  override fun getHeaderCount(): Int {
    TODO("Not yet implemented")
  }

  override fun getAllHeaderNames(): MutableSet<String> {
    TODO("Not yet implemented")
  }

  override fun getHeaderMap(): MutableMap<String, Array<String>> {
    TODO("Not yet implemented")
  }

  override fun getHeaderValues(name: String?): Array<String> {
    TODO("Not yet implemented")
  }

  override fun containsParameter(name: String?): Boolean {
    TODO("Not yet implemented")
  }

  override fun getParameterCount(): Int {
    TODO("Not yet implemented")
  }

  override fun getAllParameterNames(): MutableSet<String> {
    TODO("Not yet implemented")
  }

  override fun getParameterMap(): MutableMap<String, Array<String>> {
    TODO("Not yet implemented")
  }

  override fun getParameterValues(name: String): Array<String> {
    return (request.queryParameters.getAll(name) ?: emptyList()).toTypedArray()
  }

  override fun containsCookie(name: String?): Boolean {
    TODO("Not yet implemented")
  }

  override fun getCookieCount(): Int {
    TODO("Not yet implemented")
  }

  override fun getAllCookieNames(): MutableSet<String> {
    TODO("Not yet implemented")
  }

  override fun getCookieMap(): MutableMap<String, Array<String>> {
    TODO("Not yet implemented")
  }

  override fun getCookieValues(name: String?): Array<String> {
    TODO("Not yet implemented")
  }
}
