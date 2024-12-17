package de.jakobschaefer.graphql

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.EscapingStrategy
import com.github.jknack.handlebars.Handlebars
import graphql.schema.idl.SchemaParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class GraphQlPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val graphql = project.extensions.create("graphql", GraphQlExtension::class.java)
    project.tasks.create("generateGraphQlSchema") {
      inputs.file(graphql.schemaFile.get())
      outputs.dir(project.layout.buildDirectory.dir("generated-kotlin-schema/${graphql.serviceName.get()}"))
      doLast {
        val schema = SchemaParser().parse(graphql.schemaFile.asFile.get())
        val outputFile =
          project.file(
            project.layout.buildDirectory.file(
              "generated-kotlin-schema/${graphql.serviceName.get()}/${graphql.packageName.get().replace(".", "/")}/${graphql.outputFile.get()}"))
        outputFile.parentFile.mkdirs()
        val hbs = Handlebars().with(EscapingStrategy.NOOP)

        hbs.registerHelper("up") { name: String, _ -> name.replaceFirstChar { it.uppercaseChar() } }
        hbs.registerHelper("class") { typeName: String, _ -> if (typeName.contains('?')) {
          "${typeName.substringBeforeLast('?')}::class.java"
        } else {
          "${typeName}::class.java"
        } }
        hbs.registerHelper("isScalarType") { name: String, _ ->
          name == "String" ||
              name == "Uuid" ||
              name == "Int" ||
              name == "Float" ||
              name == "Currency" ||
              name == "BigInt" ||
              name == "BigDecimal"
        }
        val ctx =
          Context.newBuilder(
            mapOf(
              "packageName" to graphql.packageName.get(),
            ))
            .combine(mapOf("schema" to GqlSchemaContext(schema)))
            .build()
        val output =
          hbs.compileInline(
            """
      @file:OptIn(ExperimentalUuidApi::class)
      
      package {{ packageName }}
      
      import de.jakobschaefer.graphql.GraphQlSchemaWiring
      import graphql.GraphQLError
      import graphql.execution.DataFetcherResult
      import graphql.execution.ResultPath
      import graphql.schema.DataFetchingEnvironment
      import graphql.schema.idl.TypeRuntimeWiring
      import io.ktor.server.routing.*
      import io.ktor.utils.io.KtorDsl
      import kotlinx.coroutines.CoroutineScope
      import kotlinx.coroutines.SupervisorJob
      import kotlinx.coroutines.async
      import kotlinx.coroutines.future.asCompletableFuture
      import kotlin.uuid.ExperimentalUuidApi
      import kotlin.uuid.Uuid
      import java.util.*
      
      {{#each schema.types}}
      // --------------------------- {{ typeName }} ------------------------------
      data class GraphQl{{ typeName }}(
      {{#each fields}}  val {{fieldName}}: {{fieldTypeName}},
      {{/each}})
      
      @KtorDsl
      fun GraphQlSchemaWiring.type{{ typeName }}(spec: GraphQl{{ typeName }}Wiring.() -> Unit) {
        runtimeWiring.type("{{ typeName }}") { builder ->
          GraphQl{{ typeName }}Wiring(builder).apply(spec).typeWiring
        }
      }

      class GraphQl{{ typeName }}Wiring(
        val typeWiring: TypeRuntimeWiring.Builder
      )
      
      {{#each fields}}
      @KtorDsl
      fun GraphQl{{ typeName }}Wiring.resolve{{up fieldName }}(
        resolveFn: suspend RoutingContext.(
          DataFetchingEnvironment,
          {{#each inputs}}{{fieldTypeName}},
          {{/each}}) -> {{ fieldTypeName }}
        ) {
          typeWiring.dataFetcher("{{ fieldName }}") { env ->
            {{#each inputs}}
            // val {{ fieldName }}Arg = GraphQlSchemaWiring.gson.toJson(env.getArgument("{{ fieldName }}"))
            // val {{ fieldName }}: {{ fieldTypeName }} = GraphQlSchemaWiring.gson.fromJson({{ fieldName }}Arg, {{class fieldTypeName }})
            val {{ fieldName }}: {{ fieldTypeName }} = GraphQlSchemaWiring.parseArgument(env.getArgument("{{ fieldName }}"), {{class fieldTypeName }})
            {{/each}}
            val coroutineScope = env.graphQlContext.get<CoroutineScope>("coroutineScope")
            val routingContext = env.graphQlContext.get<RoutingContext>("routingContext")
            coroutineScope.async(SupervisorJob()) {
              routingContext.resolveFn(
                env,
                {{#each inputs}}{{ fieldName }},
                {{/each}})
            }.asCompletableFuture()
          }
      }
      {{/each}}{{/each}}
      // ---------------------------------- Enums -----------------------------
      
      {{#each schema.enums}}
      enum class GraphQl{{ typeName }} {
      {{#each values}}
        {{ this }},{{/each}}
      }{{/each}}
      
      // ---------------------------------- Inputs -----------------------------
      {{#each schema.inputs}}
      data class GraphQl{{ typeName }}(
      {{#each fields}}  val {{ fieldName }}: {{ fieldTypeName }},
      {{/each}}) {
        companion object {
          fun fromMap(map: Map<String, Any>) = GraphQl{{ typeName }}(
            {{#each fields}}{{ fieldName }} = map["{{ fieldName }}"] as {{ fieldTypeName }},
            {{/each}}
          )
        }
      }
      {{/each}}
    """
              .trimIndent())
            .apply(ctx)
        outputFile.writeText(output)
      }
    }

    project.tasks.named("compileKotlin") {
      dependsOn("generateGraphQlSchema")
    }

    val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
    sourceSets.configureEach {
      java.srcDir(project.tasks.named("generateGraphQlSchema"))
    }
  }
}

