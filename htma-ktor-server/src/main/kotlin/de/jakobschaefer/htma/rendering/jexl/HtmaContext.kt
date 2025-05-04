package de.jakobschaefer.htma.rendering.jexl

import de.jakobschaefer.htma.HtmaConfiguration
import de.jakobschaefer.htma.graphql.GraphQlParamsToVariablesConverter
import de.jakobschaefer.htma.graphql.GraphQlRequest
import de.jakobschaefer.htma.rendering.HtmaState
import de.jakobschaefer.htma.routing.HtmaParams
import de.jakobschaefer.htma.rendering.GRAPHQL_MUTATION_PARAMETER_NAME
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
  val location: String,
  val configuration: HtmaConfiguration,
) : MapContext() {
  val itStack = ArrayDeque<Any?>()

  init {
    set("locale", locale)
    set("params", params)
    set("htma", htmaState)
    set("location", location)
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
        htmaState.toPage.buildOutletChainListStartingFrom(htmaState.outletSwap.innerMostCommonOutlet)
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
        val mutations = configuration.appManifest.graphQlDocuments[htmaState.toPage.templateName]?.mutations ?: emptyList()
        val operationName = params[GRAPHQL_MUTATION_PARAMETER_NAME]
        if (operationName != null) {
          val mutation = mutations.find { it.operationName == operationName[0] }
          if (mutation != null) {
            val request = GraphQlRequest(
              query = mutation.operation,
              operationName = mutation.operationName,
              variables = GraphQlParamsToVariablesConverter.convert(params)
            )
            val result = configuration.graphQlService.execute(request)
            set("mutation", result)
          } else {
            log.error("Could not find mutation with operationName {}. Available mutations: {}", operationName, mutations.map { it.operationName })
            set("mutation", emptyMap<String, Any>())
          }

        } else if (mutations.isEmpty()) {
          // Nothing to do. Skip the execution.
        } else if (mutations.size == 1) {
          val mutation = mutations.first()
          val request = GraphQlRequest(
            query = mutation.operation,
            operationName = mutation.operationName,
            variables = GraphQlParamsToVariablesConverter.convert(params)
          )
          val result = configuration.graphQlService.execute(request)
          set("mutation", result)
        } else {
          log.error("Could not decide which mutation to execute, because no operationName was provided. Please provide one via the data-x-operation attribute. Available mutations: {}", mutations.map { it.operationName })
          set("mutation", emptyMap<String, Any>())
        }
      }
    }
    set("mutation", emptyMap<String, Any>())
  }

  companion object {
    private val log = LoggerFactory.getLogger(HtmaContext::class.java)
  }
}
