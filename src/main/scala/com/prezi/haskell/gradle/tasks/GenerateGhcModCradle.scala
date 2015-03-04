package com.prezi.haskell.gradle.tasks

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

class GenerateGhcModCradle extends DefaultTask {

  var configuration: Option[Configuration] = None
  private var targetFile_ : Option[File] = None

  def targetFile = targetFile_
  def targetFile_=(value: Option[File]): Unit = {
    targetFile_ = value

    if (value.isDefined) {
      getOutputs.file(value.get)
    }
  }

  @TaskAction
  def run(): Unit = {
    if (!configuration.isDefined) {
      throw new IllegalStateException("configuration is not specified")
    }

    if (!targetFile.isDefined) {
      throw new IllegalStateException("targetFile is not specified")
    }

    val deps = configuration.get.getResolvedConfiguration.getResolvedArtifacts
      .asScala
      .map(Sandbox.fromResolvedArtifact(getProject, _))
      .toList

    val sandbox = getProject.getExtensions.getByType(classOf[Sandbox])

    val cradleFile = generateContent(deps ::: List(sandbox))
    Files.write(targetFile.get.toPath, cradleFile.getBytes(StandardCharsets.UTF_8))
  }

  private def generateContent(sandboxes: List[Sandbox]): String = {
    val content = new StringBuilder()

    for (sandbox <- sandboxes) {
      content.append(sandbox.packageDb.getAbsolutePath)
      content.append('\n')
    }

    content.mkString
  }
}
