package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.model.{SandboxArtifact, ProjectSandboxStore, SandboxStore}
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{Configuration, ResolvedDependency}
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters._

class StoreDependentSandboxes extends DefaultTask with HaskellDependencies {

  dependsOn("copySandFix")

  var sandFixPath: Option[File] = None

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet

    getLogger.info("Storing dependent sandboxes for {}", getProject.getName)
    for (dependency <- configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala) {
      storeDependency(dependency)
    }
  }

  def storeDependency(dependency: ResolvedDependency, prefix: String = ""): Set[SandboxArtifact] = {
    getLogger.info("{}Dependency {}", prefix, dependency.getName)
    val depSandboxes =
      (for (child <- dependency.getChildren.asScala;
            childDep <- storeDependency(child, prefix + "  "))
       yield childDep).toSet

    val sandboxes = dependency.getAllModuleArtifacts
      .asScala
      .map(artifact => new SandboxArtifact(artifact.getName, artifact.getFile))
      .toSet
    for (sandbox <- sandboxes) {
      store.store(sandbox, depSandboxes)
    }

    sandboxes
  }
}