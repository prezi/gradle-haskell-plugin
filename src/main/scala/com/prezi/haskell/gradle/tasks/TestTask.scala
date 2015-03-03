package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters._

/**
 * Executes cabal test with the proper sandbox chaining
 */
class TestTask extends DefaultTask {
  var configuration: Option[Configuration] = None
  var tools: Option[HaskellTools] = None

  @TaskAction
  def run(): Unit = {
    if (!configuration.isDefined) {
      throw new IllegalStateException("configuration is not specified")
    }

    if (!tools.isDefined) {
      throw new IllegalStateException("tools is not specified")
    }

    val deps = configuration.get.getResolvedConfiguration.getResolvedArtifacts
      .asScala
      .map(Sandbox.fromResolvedArtifact(getProject, _))
      .toList

    val sandbox = getProject.getExtensions.getByType(classOf[Sandbox])

    tools.get.cabalTest(getProject.getProjectDir, new File(getProject.getBuildDir, "dist"), sandbox, deps)
  }
}
