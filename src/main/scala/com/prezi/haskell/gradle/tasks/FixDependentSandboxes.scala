package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{ResolvedArtifact, ResolvedDependency}
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._
import scala.collection.immutable.Set

/**
 * Applies SandFix on all the extracted dependent sandboxes
 */
class FixDependentSandboxes extends DefaultTask with HaskellDependencies with UsingHaskellTools {
  dependsOn("extractDependentSandboxes")
  dependsOn("copySandFix")

  var sandFixPath: Option[File] = None

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    for (dep <- configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala) {
      fixDependency(dep)
    }
  }

  private def finalSandFixPath = sandFixPath.getOrElse(getProject.getBuildDir </> "sandfix/sandfix-1.1")

  private def fixDependency(dep: ResolvedDependency): Set[Sandbox] = {
    val childSandboxes =
      dep.getChildren.asScala
        .map(fixDependency)
        .fold(Set())(_.union(_))

    val sandboxes : Set[Sandbox] =
      for (artifact <- dep.getModuleArtifacts.asScala.toSet[ResolvedArtifact])
      yield Sandbox.fromResolvedArtifact(getProject, artifact)

    for (sandbox <- sandboxes) {
      if (isSandboxDirty(sandbox)) {
        getLogger.info("Fixing sandbox at {}", sandbox)

        runSandFix(sandbox, childSandboxes.toList)
        tools.get.ghcPkgRecache(sandbox)
        (sandbox.root </> "fixed").createNewFile()
      } else {
        getLogger.info("Sandbox at {} is not dirty, no need to fix", sandbox)
      }
    }

    sandboxes
  }

  private def runSandFix(sandbox: Sandbox, others: List[Sandbox]): Unit = {
    val dbArgs = others.map(child => child.asPackageDbArg)

    tools.get.runHaskell(
      finalSandFixPath </> "SandFix.hs",
      List( sandbox.root.getAbsolutePath,
        "packages",
        "--package-db=global")
        ::: dbArgs.toList : _*)
  }

  private def isSandboxDirty(sandbox: Sandbox): Boolean =
    !(sandbox.root </> "fixed").exists()
}
