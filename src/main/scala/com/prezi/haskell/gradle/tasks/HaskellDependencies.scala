package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.{SandboxArtifact, Sandbox, SandboxStore}
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import scala.collection.JavaConverters._

/**
 * Mixin for tasks relying on haskell dependencies
 */
trait HaskellDependencies {
  this: Task =>

  private var _configuration: Option[Configuration] = None
  def configuration = _configuration
  def configuration_= (value: Option[Configuration]): Unit = {
    _configuration match {
      case Some(cfg) => throw new IllegalStateException("configuration was already set to " + cfg.getName)
      case _ =>
    }

    _configuration = value

    _configuration match {
      case Some(cfg) => onConfigurationSet(cfg)
      case _ =>
    }
  }

  protected def onConfigurationSet(cfg: Configuration): Unit = {
  }

  protected def needsConfigurationSet: Unit = {
    if (!configuration.isDefined) {
      throw new IllegalStateException("configuration is not specified")
    }
  }

  protected def store: SandboxStore = getProject.getExtensions.findByName("sandboxStore").asInstanceOf[SandboxStore]

  protected def dependentSandboxes: List[Sandbox] =
    configuration.get.getResolvedConfiguration.getResolvedArtifacts
      .asScala
      .map(artifact => store.find(new SandboxArtifact(artifact.getName, artifact.getFile)))
      .toList
}
