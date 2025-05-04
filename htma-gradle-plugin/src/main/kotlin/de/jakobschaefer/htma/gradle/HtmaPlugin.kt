package de.jakobschaefer.htma.gradle

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpmTask
import de.jakobschaefer.htma.webinf.*
import graphql.language.AstPrinter
import graphql.language.OperationDefinition
import graphql.parser.Parser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.internal.cc.base.logger
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

class HtmaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.apply(plugin = "com.github.node-gradle.node")
    val htma = project.extensions.create("htma", HtmaExtension::class.java)

    project.configure<NodeExtension> {
      download.set(true)
      version.set("20.16.0")
    }

    project.tasks.register("buildAppManifest") {
      group = "htma"
      val outputDir = project.layout.buildDirectory.dir("htma")
      val webDir = htma.webDir.get()
      inputs.dir(webDir)
      outputs.file(outputDir.get().file("manifest.json"))
      val projectPath = project.projectDir.path
      doLast {
        val webPath = Paths.get(projectPath, webDir)
        val appManifest = buildAppManifest(webPath)
        val appManifestContent = appManifest.toJson()
        outputDir.get().file("manifest.json").asFile.writeText(appManifestContent)
      }
    }

    project.tasks.register("npmRunDev", NpmTask::class.java) {
      group = "htma"
      dependsOn("npmInstall")
      description = "Executes 'npm run dev'"
      args.set(listOf("run", "dev"))
      inputs.files("package.json", "package-lock.json", "vite.config.js")
      inputs.dir("web")
      inputs.dir(project.fileTree("node_modules").exclude(".cache"))
    }

    val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer
    project.tasks.register("serverRunDev", JavaExec::class.java) {
      group = "htma"
      classpath = sourceSets.getByName("main").runtimeClasspath
      mainClass.set("io.ktor.server.netty.EngineMain")
      inputs.files("src/main/kotlin")
      setJvmArgs(listOf("-Dio.ktor.development=true"))
      workingDir(project.projectDir)
    }

    project.tasks.register("serverRunProd", JavaExec::class.java) {
      group = "htma"
      classpath = sourceSets.getByName("main").runtimeClasspath
      mainClass.set("io.ktor.server.netty.EngineMain")
      inputs.files("src/main/kotlin")
      workingDir(project.projectDir)
    }

    project.tasks.register("npmRunBuild", NpmTask::class.java) {
      group = "htma"
      dependsOn("npmInstall")
      description = "Executes npm run build"
      args.set(listOf("run", "build"))
      inputs.files("package.json", "package-lock.json", "vite.config.js")
      inputs.dir("web")
      inputs.dir(project.fileTree("node_modules").exclude(".cache"))
      outputs.dir("dist")
    }
    project.tasks.named("build") { dependsOn("npmRunBuild") }
    project.tasks.named("clean", Delete::class.java) { delete("dist") }

    project.tasks.withType<ProcessResources>().configureEach {
      val resourceBase = htma.resourceBase.get().let {
        if (it.startsWith("/")) {
          it.substringAfter("/")
        } else {
          it
        }
      }
      val webDir = htma.webDir.get()
      from(project.tasks.named("npmRunBuild")) { into(resourceBase) }
      from(project.tasks.named("buildAppManifest")) { into(resourceBase) }
      from(webDir) {
        include("**/*.html")
        include("**/*.properties")
        into("$resourceBase/web")
      }
    }
  }

  private fun buildAppManifest(webDir: Path): AppManifest {
    val htmlFiles = Files.walk(webDir)
      .filter { it.pathString.endsWith(".html") }
      .map { htmlFile ->
        htmlFile.pathString
          .substringAfter(webDir.pathString)
          .substringBeforeLast(".html")
          .replace(FileSystems.getDefault().separator, "/")
      }.toList()

    val pagesAndLayouts = htmlFiles
      .filter { htmlFile -> !htmlFile.startsWith("/__components") }
      .map { HtmlFile(it) }

      val pages = buildList {
        for (htmlFile in pagesAndLayouts) {
          if (htmlFile.isLayout) {
            continue
          }

          val page = AppManifestPage(
            filePath = htmlFile.path + ".html",
            remotePath = htmlFile.remotePath,
            canonicalPath = "__root." + htmlFile.canonicalPathWithoutRoot,
            templateName = htmlFile.templateName,
            outletChain = buildMap {
              var currentOutlet = "__root"
              for (outlet in htmlFile.canonicalOutletChain) {
                val templateName = when (val page = pagesAndLayouts.find { it.canonicalPathWithoutRoot == outlet }) {
                  null -> outlet // Expect a template at the canonical path.
                  else -> page.templateName
                }
                put(currentOutlet, templateName)
                currentOutlet = templateName
              }
            }
          )
          add(page)
        }
      }

    val components = htmlFiles
      .filter { htmlFile -> htmlFile.startsWith("/__components") }
      .map { AppComponent(it.substringAfter("/__components/")) }
      .toList()

    val graphQlDocumentParser = Parser()
    val graphQlDocuments = Files.walk(webDir)
      .filter { it.pathString.endsWith(".graphql") }
      .toList()
      .mapNotNull { graphQlFile ->
        try {
          graphQlFile to graphQlDocumentParser.parseDocument(graphQlFile.toFile().readText(Charsets.UTF_8))
        } catch (e: Exception) {
          logger.error("Failed to parse GraphQL file {}. This file will be ignored. Reason: {}", graphQlFile.pathString, e.message)
          null
        }
      }
      .associate { (graphQlFile, parsedDocument) ->
        val templateName = graphQlFile.pathString
          .substringAfter(webDir.pathString)
          .substringBeforeLast(".graphql")
          .replace(FileSystems.getDefault().separator, "/")
          .substring(1)

        val queries = parsedDocument.definitions.filterIsInstance<OperationDefinition>()
          .filter { it.operation == OperationDefinition.Operation.QUERY }
          .map {
            AppManifestGraphQlOperation(
              operationName = it.name,
              operation = AstPrinter.printAst(it),
            )
          }
        val mutations = parsedDocument.definitions.filterIsInstance<OperationDefinition>()
          .filter { it.operation == OperationDefinition.Operation.MUTATION }
          .map {
            AppManifestGraphQlOperation(
              operationName = it.name,
              operation = AstPrinter.printAst(it),
            )
          }
        val document = AppManifestGraphQlDocument(
          queries = queries,
          mutations = mutations
        )
        (templateName to document)
      }

    return AppManifest(
      pages = pages,
      components = components,
      graphQlDocuments = graphQlDocuments
    )
  }
}

data class HtmlFile(
  val path: String
) {
  val templateName = path.substring(1)

  val canonicalPathWithoutRoot = path.substring(1).replace('/', '.')

  val canonicalPathSegments = canonicalPathWithoutRoot
    .split('.')

  val isLayout = canonicalPathSegments.last().startsWith('_')

  val isIndex = canonicalPathSegments.last() == "index"

  val remotePathSegments = canonicalPathSegments
    .filter { !it.startsWith('_') }
    .map {
      if (it.startsWith('$')) {
        "{${it.substring(1)}}"
      } else {
        it
      }
    }
  val remotePath = "/" + if (isIndex) {
    remotePathSegments.dropLast(1).joinToString("/")
  } else {
    remotePathSegments.joinToString("/")
  }

  val canonicalOutletChain = buildList {
    for (i in canonicalPathSegments.indices) {
      val segment = canonicalPathSegments[i]
      if (segment.startsWith("_")) {
        val canonicalLayoutPath = canonicalPathSegments.subList(0, i + 1).joinToString(".")
        add(canonicalLayoutPath)
      }
    }

    if (!isLayout) {
      add(canonicalPathSegments.joinToString("."))
    }
  }
}
