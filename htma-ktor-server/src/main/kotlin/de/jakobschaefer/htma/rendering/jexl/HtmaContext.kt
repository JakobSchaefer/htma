package de.jakobschaefer.htma.rendering.jexl

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.graphql.GraphQlParamsToVariablesConverter
import de.jakobschaefer.htma.graphql.GraphQlRequest
import de.jakobschaefer.htma.rendering.HtmaState
import de.jakobschaefer.htma.routing.HtmaParams
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.jexl3.MapContext
import org.slf4j.LoggerFactory
import java.util.Locale

internal class HtmaContext(
  val call: RoutingCall,
  val locale: Locale,
  val htmaState: HtmaState,
  val params: HtmaParams,
  val configuration: HtmaConfiguration
) : MapContext() {
  val itStack = ArrayDeque<Any?>()

  init {
    set("locale", locale)
    set("params", params)
    set("htma", htmaState)
  }

  fun pushIt(it: Any?) {
    itStack.addLast(it)
  }

  fun popIt() {
    itStack.removeLast()
  }

  override fun get(name: String): Any? {
    return if (name == "it") {
      try {
        itStack.last()
      } catch (e: NoSuchElementException) {
        super.get(name)
      }
    } else {
      super.get(name)
    }
  }

  suspend fun executeQueries() {
    val graphQlService = configuration.graphQlService
    if (graphQlService != null) {
      val outletChainList = if (htmaState.outletSwap != null) {
        if (htmaState.outletSwap.innerMostCommonOutlet != htmaState.outletSwap.newOutlet) {
          htmaState.toPage.buildOutletChainListStartingFrom(htmaState.outletSwap.innerMostCommonOutlet)
            .drop(1) // Do not execute layout queries, the layout itself will not be exchanged
        } else {
          htmaState.toPage.buildOutletChainListStartingFrom(htmaState.outletSwap.innerMostCommonOutlet)
        }
      } else {
        htmaState.toPage.outletChainList
      }
      val queries = outletChainList
        .mapNotNull { configuration.appManifest.graphQlDocuments[it]?.queries }.flatten()
      val executedQueries = coroutineScope {
        queries.map {
          async {
            val request = GraphQlRequest(
              query = it.operation,
              operationName = it.operationName,
              variables = GraphQlParamsToVariablesConverter.convert(params)
            )
            val response = graphQlService.execute(request)
            it.operationName to response
          }
        }.awaitAll()
      }.toMap()
      set("query", executedQueries)
    } else {
      set("query", emptyMap<String, Any>())
    }
  }

  suspend fun executeMutationsIfRequired() {
    if (call.request.httpMethod == HttpMethod.Post) {
      if (configuration.graphQlService != null) {
        val mutation = configuration.appManifest.graphQlDocuments[htmaState.toPage.templateName]?.mutation
        if (mutation != null) {
          val request = GraphQlRequest(
            query = mutation.operation,
            operationName = mutation.operationName,
            variables = GraphQlParamsToVariablesConverter.convert(params)
          )
          val result = configuration.graphQlService.execute(request)
          set("mutation", result)
          return
        }
      }
    }
    set("mutation", emptyMap<String, Any>())
  }

  companion object {
    private val log = LoggerFactory.getLogger(HtmaContext::class.java)
  }
}
