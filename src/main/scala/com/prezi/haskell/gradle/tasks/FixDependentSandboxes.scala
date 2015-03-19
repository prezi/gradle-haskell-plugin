package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{ResolvedArtifact, ResolvedDependency}
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._
import scala.collection.immutable.Set

/**
 * Applies SandFix on all the extracted dependent sandboxes
 */
class FixDependentSandboxes extends DefaultTask with HaskellDependencies with UsingHaskellTools with HaskellProjectSupport {
  dependsOn("extractDependentSandboxes")
  dependsOn("copySandFix")

  var sandFixPath: Option[File] = None

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    dumpDependencies()

    fixChildren(configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala.toList, Map.empty)
  }

  private def finalSandFixPath = sandFixPath.getOrElse(getProject.getBuildDir </> "sandfix/sandfix-1.1")

  private def merge(a: Map[ResolvedDependency, Set[Sandbox]], b: Map[ResolvedDependency, Set[Sandbox]]): Map[ResolvedDependency, Set[Sandbox]] = {
    b.foldLeft(a) { case (r, (key, value)) => r.updated(key, value) }
  }

  private def fixChildren(children: List[ResolvedDependency], alreadyFixed: Map[ResolvedDependency, Set[Sandbox]]): Map[ResolvedDependency, Set[Sandbox]] = {
    children match {
      case child::rest =>
        val childDeps = child.getChildren.asScala.toList
        val result = fixChildren(childDeps, alreadyFixed)
        val newAlreadyFixed = merge(alreadyFixed, result)

        if (!newAlreadyFixed.contains(child)) {
          val childDepSandboxes = childDeps.map(newAlreadyFixed).fold(Set.empty)(_.union(_))
          val childResultSandBoxes = fixChild(child, childDepSandboxes)
          fixChildren(rest, newAlreadyFixed.updated(child, childResultSandBoxes union childDepSandboxes))
        } else {
          fixChildren(rest, newAlreadyFixed)
        }

      case List() => alreadyFixed
    }
  }

  private def fixChild(dep: ResolvedDependency, childSandboxes: Set[Sandbox]): Set[Sandbox] = {
    getLogger.info("Fixing dependency {}", dep)

    val sandboxes : Set[Sandbox] =
      for (artifact <- dep.getModuleArtifacts.asScala.toSet[ResolvedArtifact])
        yield Sandbox.fromResolvedArtifact(getProject, artifact)

    for (sandbox <- sandboxes) {
      getLogger.info("Fixing sandbox at {}", sandbox)

      sandbox.root.mkdirs()
      getProject.copy(asClosure { spec : CopySpec =>
        spec.from (sandbox.extractionRoot)
        spec.into (sandbox.root)
      })

      runSandFix(sandbox, childSandboxes.toList)
      tools.get.ghcPkgRecache(haskellExtension.getEnvConfigurer, sandbox)
    }

    sandboxes
  }

  private def runSandFix(sandbox: Sandbox, others: List[Sandbox]): Unit = {
    val dbArgs = others.map(child => child.asPackageDbArg)

    tools.get.runHaskell(
      haskellExtension.getEnvConfigurer,
      finalSandFixPath </> "SandFix.hs",
      List( sandbox.root.getAbsolutePath,
        "packages",
        "--package-db=global")
        ::: dbArgs.toList : _*)
  }

  private def dumpDependencies(): Unit = {
    getLogger.debug("configuration {} resolution details:", configuration.get.getName)
    for (dep <- configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala) {

      def dumpDep(dep: ResolvedDependency, prefix: String = ""): Unit = {
        getLogger.debug("{}dependency {}", prefix, dep.getName)

        for (child <- dep.getChildren.asScala) {
          dumpDep(child, prefix + "    ")
        }
      }

      dumpDep(dep)
    }
  }
}
