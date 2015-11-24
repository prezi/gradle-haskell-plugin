package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ModuleId
import com.prezi.haskell.gradle.model.sandboxstore.SandboxStore
import com.prezi.haskell.gradle.model.{Sandbox, SandboxArtifact}
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.{DependencyResult, ResolvedDependencyResult}

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
    if (configuration.isEmpty) {
      throw new IllegalStateException("configuration is not specified")
    }
  }

  protected def store: SandboxStore = getProject.getExtensions.findByName("sandboxStore").asInstanceOf[SandboxStore]

  private def collectSandboxes(node: DependencyResult, results: List[ComponentIdentifier]): List[ComponentIdentifier] = {
    node match {
      case resolvedDep: ResolvedDependencyResult =>
        val childDeps = resolvedDep.getSelected.getDependencies.asScala
        val childIds = childDeps.foldLeft(List[ComponentIdentifier]()) { (r, d) => collectSandboxes(d, r) }
        results ::: childIds ::: List(resolvedDep.getSelected.getId)
      case _ =>
        results
    }
  }

  protected def dependentSandboxes: List[Sandbox] = {
    val result = configuration.get.getIncoming.getResolutionResult
    val orderedIds = result.getRoot.getDependencies
      .asScala
      .foldLeft(List[ComponentIdentifier]()) { (results, dep) => collectSandboxes(dep, results) }
      .distinct

    val artifacts = configuration.get.getResolvedConfiguration.getResolvedArtifacts
      .asScala
      .map(artifact => (ModuleId.fromModuleVersionIdentifier(artifact.getModuleVersion.getId), artifact.getFile))
      .toMap

    val zips =
      for (id <- orderedIds;
           modId = ModuleId.fromComponentIdentifier(getProject.getRootProject, id))
      yield artifacts.get(modId) match {
        case Some(file) =>
          val sandbox = store.find(new SandboxArtifact(modId.name, file))
          if (sandbox.packageDb.exists()) {
            Some(sandbox)
          } else {
            None
          }
        case None => None
      }

    zips.filter(_.isDefined).map(_.get)
  }
}
