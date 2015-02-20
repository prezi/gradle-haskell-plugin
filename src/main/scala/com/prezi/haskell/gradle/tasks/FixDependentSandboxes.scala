package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{Configuration, ResolvedArtifact, ResolvedDependency}
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._
import scala.collection.immutable.Set

/**
 * Applies SandFix on all the extracted dependent sandboxes
 */
class FixDependentSandboxes extends DefaultTask {
  dependsOn("extractDependentSandboxes")
  dependsOn("copySandFix")

  var configuration: Option[Configuration] = None
  var tools: Option[HaskellTools] = None

  var sandFixPath: Option[File] = None

  @TaskAction
  def run(): Unit = {
    if (!configuration.isDefined) {
      throw new IllegalStateException("configuration is not specified")
    }
    if (!tools.isDefined) {
      throw new IllegalStateException("tools is not defined")
    }

    for (dep <- configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala) {
      fixDependency(dep)
    }
  }

  private def finalSandFixPath = sandFixPath.getOrElse(new File(getProject.getBuildDir, "sandfix/sandfix-1.1"))

  private def fixDependency(dep: ResolvedDependency): Set[Sandbox] = {
    val childSandboxes =
      dep.getChildren.asScala
        .map(fixDependency)
        .fold(Set())(_.union(_))

    val sandboxes : Set[Sandbox] =
      for (artifact <- dep.getModuleArtifacts.asScala.toSet[ResolvedArtifact])
      yield Sandbox.fromResolvedArtifact(getProject, artifact)

    for (sandbox <- sandboxes) {
      getLogger.info("Fixing sandbox at {}", sandbox)

      runSandFix(sandbox, childSandboxes.toList)
      tools.get.ghcPkgRecache(sandbox)
    }

    sandboxes
  }

  private def runSandFix(sandbox: Sandbox, others: List[Sandbox]): Unit = {
    val dbArgs = others.map(child => child.asPackageDbArg)

    tools.get.runHaskell(
      new File(finalSandFixPath, "src/SandFix.hs"),
      List( sandbox.root.getAbsolutePath,
        "packages",
        "--package-db=global")
        ::: dbArgs.toList : _*)

  }
}
