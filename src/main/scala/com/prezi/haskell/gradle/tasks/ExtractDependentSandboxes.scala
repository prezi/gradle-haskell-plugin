package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters._

import com.prezi.haskell.gradle.ApiHelper._

/**
 * Extracts a project's dependencies as custom sandboxes
 */
class ExtractDependentSandboxes extends DefaultTask {

  private var _configuration: Option[Configuration] = None
  def configuration = _configuration
  def configuration_= (value: Option[Configuration]): Unit = {
    _configuration match {
      case Some(cfg) => throw new IllegalStateException("configuration was already set to " + cfg.getName)
      case _ =>
    }

    _configuration = value

    _configuration match {
      case Some(cfg) => dependsOn(cfg)
      case _ =>
    }
  }

  @TaskAction
  def run(): Unit = {
    if (!configuration.isDefined) {
      throw new IllegalStateException("configuration is not set")
    }

    for (artifact <- configuration.get.getResolvedConfiguration.getResolvedArtifacts.asScala) {
      val sandbox = Sandbox.fromResolvedArtifact(getProject, artifact)

      getLogger.trace("Extracting sandbox dependency {}", artifact.getFile.getName)

      sandbox.root.mkdirs()
      getProject.copy(asClosure { spec : CopySpec =>
        spec.from (getProject.zipTree(artifact.getFile))
        spec.into (sandbox.root)
      })
    }
  }
}
