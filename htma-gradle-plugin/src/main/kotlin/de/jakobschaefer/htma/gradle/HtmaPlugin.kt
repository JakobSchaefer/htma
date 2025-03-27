package de.jakobschaefer.htma.gradle

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpxTask
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.AppManifestPage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
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

    project.tasks.register("npxViteBuild", NpxTask::class.java) {
      dependsOn("npmInstall")
      description = "Executes npx vite build"
      command.set("vite")
      args.set(listOf("build"))
      inputs.files("package.json", "package-lock.json", "vite.config.js")
      inputs.dir("web")
      inputs.dir(project.fileTree("node_modules").exclude(".cache"))
      outputs.dir("dist")
    }
    project.tasks.named("build") { dependsOn("npxViteBuild") }
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
      from(project.tasks.named("npxViteBuild")) { into(resourceBase) }
      from(project.tasks.named("buildAppManifest")) { into(resourceBase) }
      from(webDir) {
        include("**/*.html")
        include("**/*.properties")
        include("**/*.graphql")
        into("$resourceBase/web")
      }
    }
  }

  private fun buildAppManifest(webDir: Path): AppManifest {
    val htmlFiles = Files.walk(webDir)
      .filter { it.pathString.endsWith(".html")}
      .map { htmlFile ->
        htmlFile.pathString.substringAfter(webDir.pathString).substringBeforeLast(".html")
      }
      .map { HtmlFile(it) }
      .toList()

      val pages = buildList {
        for (htmlFile in htmlFiles) {
          if (htmlFile.isLayout) {
            continue
          }

          val page = AppManifestPage(
            remotePath = htmlFile.remotePath,
            outlets = buildMap {
              var currentOutlet = "__root"
              for (outlet in htmlFile.normalizedOutletChain) {
                val templateName = htmlFiles.find { it.normalizedPath == outlet }!!.templateName
                put(currentOutlet, templateName)
                currentOutlet = outlet
              }
            }
          )
          add(page)
        }
      }

    return AppManifest(
      pages = pages,
    )
  }
}

data class HtmlFile(
  val path: String
) {
  val templateName = path.substring(1)

  val normalizedPath = path.substring(1)
    .replace('/', '.')

  val normalizedPathSegments = normalizedPath
    .split('.')

  val isLayout = normalizedPathSegments.last().startsWith('_')

  val isIndex = normalizedPathSegments.last() == "index"

  val canonicalPathSegments = normalizedPathSegments
    .filter { !it.startsWith('_') }
    .map {
      if (it.startsWith('$')) {
        "{${it.substring(1)}}"
      } else {
        it
      }
    }
  val remotePath = "/" + if (isIndex) {
    canonicalPathSegments.dropLast(1).joinToString("/")
  } else {
    canonicalPathSegments.joinToString("/")
  }

  val normalizedOutletChain = buildList {
    for (i in normalizedPathSegments.indices) {
      val segment = normalizedPathSegments[i]
      if (segment.startsWith("_")) {
        val normalizedLayoutPath = normalizedPathSegments.subList(0, i + 1).joinToString(".")
        add(normalizedLayoutPath)
      }
    }

    if (!isLayout) {
      add(normalizedPathSegments.joinToString("."))
    }
  }
}
