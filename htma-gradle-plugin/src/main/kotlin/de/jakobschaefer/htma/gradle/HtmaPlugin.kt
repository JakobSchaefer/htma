package de.jakobschaefer.htma.gradle

import com.apollographql.apollo.gradle.api.ApolloExtension
import com.apollographql.apollo.gradle.internal.DefaultApolloExtension
import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpxTask
import de.jakobschaefer.htma.webinf.AppManifest
import de.jakobschaefer.htma.webinf.AppManifestPage
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.pathString

class HtmaPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.apply(plugin = "com.github.node-gradle.node")
    project.apply(plugin = "com.apollographql.apollo")
    val htma = project.extensions.create("htma", HtmaExtension::class.java)

    project.configure<NodeExtension> {
      download.set(true)
      version.set("20.16.0")
    }

    project.tasks.create("buildAppManifest") {
      val outputDir = project.layout.buildDirectory.dir("htma")
      val webDir = htma.webDir.get()
      inputs.dir(webDir)
      outputs.file(outputDir.get().file("manifest.json"))
      doLast {
        val webPath = Paths.get(project.projectDir.path, webDir)
        val appManifest = buildAppManifest(webPath)
        val appManifestContent = appManifest.toJson()
        outputDir.get().file("manifest.json").asFile.writeText(appManifestContent)
      }
    }

    project.tasks.create("npxViteBuild", NpxTask::class.java) {
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
    val appManifestPages =
      Files.walk(webDir)
        .filter { it.pathString.endsWith(".html") }
        .map { htmlFile ->
          val htmlFilePathWithoutExtension =
            htmlFile.pathString.substringAfter(webDir.pathString).substringBeforeLast(".html")
          AppManifestPage(
            remotePath =
              if (htmlFilePathWithoutExtension == "/index") {
                "/"
              } else if (htmlFilePathWithoutExtension.endsWith("index")) {
                htmlFilePathWithoutExtension.substringBeforeLast("/index")
              } else {
                htmlFilePathWithoutExtension
              },
            templateName = htmlFilePathWithoutExtension.substringAfter("/"),
          )
        }
        .collect(Collectors.toList())
    return AppManifest(
      pages = appManifestPages,
    )
  }
}
