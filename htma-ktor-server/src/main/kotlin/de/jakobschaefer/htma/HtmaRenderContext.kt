package de.jakobschaefer.htma

import de.jakobschaefer.htma.graphql.GraphQlEngine
import de.jakobschaefer.htma.graphql.GraphQlOperationRef
import de.jakobschaefer.htma.graphql.GraphQlResponse
import de.jakobschaefer.htma.routing.HtmaClientNavigationContext
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.vite.ViteManifest
import org.thymeleaf.context.Context
import org.thymeleaf.context.IContext
import java.util.concurrent.ConcurrentHashMap

data class HtmaRenderContext(
  val isDevelopment: Boolean,
  val vite: ViteManifest,
  val app: AppManifest,
  val clientContext: HtmaClientNavigationContext?,
  val graphqlCache: ConcurrentHashMap<GraphQlOperationRef, GraphQlResponse>,
  val graphqlServices: Map<String, GraphQlEngine>
) {
  fun updateContext(context: Context) {
    context.setVariable("htma", this)
  }

  companion object {
    fun fromContext(context: IContext) = context.getVariable("htma") as HtmaRenderContext
  }
}
