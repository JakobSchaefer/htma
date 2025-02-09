package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlEngine
import de.jakobschaefer.htma.graphql.GraphQlOperationRef
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import io.ktor.server.routing.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.thymeleaf.context.Context
import org.thymeleaf.context.IContext
import java.util.concurrent.ConcurrentHashMap

class HtmaRoutingCall(
  private val call: RoutingCall,
) {
  val isDevelopment: Boolean
    get() = call.application.developmentMode

  val vite: ViteManifest
    get() = call.application.htma.viteManifest

  val app: AppManifest
    get() = call.application.htma.appManifest

  private val gqlEngine: GraphQlEngine
    get() = call.application.htma.graphqlEngine!!

  private val gqlExecutions = ConcurrentHashMap<GraphQlOperationRef, Deferred<Any>>()

  fun startGraphQlOperation(operation: GraphQlOperationRef, query: String) {
    if (!gqlExecutions.containsKey(operation)) {
      gqlExecutions[operation] = call.async {
        gqlEngine.execute(call, operation, query)
      }
    }
  }

  suspend fun awaitGraphQlOperation(operation: GraphQlOperationRef): Any {
    return gqlExecutions[operation]!!.await()
  }

  fun updateContext(context: Context) {
    context.setVariable("call", this)
  }

  companion object {
    fun fromContext(context: IContext) = context.getVariable("call") as HtmaRoutingCall
  }
}
