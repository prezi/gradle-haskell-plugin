package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.model.{SandBoxStoreResult, SandboxArtifact}
import com.twitter.util.Memoize
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{Configuration, ResolvedDependency}
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

class StoreDependentSandboxes extends DefaultTask with HaskellDependencies {

  dependsOn("copySandFix")

  var sandFixPath: Option[File] = None

  private var _isAnySandboxUpdated: Boolean = false

  def isAnySandboxUpdated: Boolean = _isAnySandboxUpdated

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet

    getLogger.info("Storing dependent sandboxes for {}", getProject.getName)
    val storeDependencyResults =
      for (dependency <- configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala) yield {
        storeDependency(dependency)
      }

    _isAnySandboxUpdated = storeDependencyResults.exists { _._2 == SandBoxStoreResult.Created }
  }

  def storeDependency(dependency: ResolvedDependency, prefix: String = ""): (Set[SandboxArtifact], SandBoxStoreResult) = {

    getLogger.info("{}Dependency {}", prefix, dependency.getName)

    val (depSandboxes: Set[SandboxArtifact], aggregatedStoreResult: SandBoxStoreResult) =
      foldStoreDependencyResults(
        (for {
          child <- dependency.getChildren.asScala
          storeDependencyResult <- List(storeDependency(child, prefix + "  "))
        } yield storeDependencyResult).toSet
      )

    val sandboxes = dependency.getAllModuleArtifacts
      .asScala
      .map(artifact => new SandboxArtifact(artifact.getName, artifact.getFile))
      .toSet

    val sandboxStoreResults =
      for (sandbox <- sandboxes) yield {
        memoizedStoreDependencyInStore(sandbox, depSandboxes)
      }

    (sandboxes,
      if (sandboxStoreResults.contains(SandBoxStoreResult.Created) || aggregatedStoreResult == SandBoxStoreResult.Created)
        SandBoxStoreResult.Created
      else
        SandBoxStoreResult.AlreadyExists)
  }

  def foldStoreDependencyResults(results: Set[(Set[SandboxArtifact], SandBoxStoreResult)]): (Set[SandboxArtifact], SandBoxStoreResult) =
    results.foldLeft((Set[SandboxArtifact](), SandBoxStoreResult.AlreadyExists.asInstanceOf[SandBoxStoreResult])) {
      case ((accSandboxes, accStoreResult), (sandbox, storeResult)) =>
        (accSandboxes ++ sandbox,
          if (storeResult == SandBoxStoreResult.Created) SandBoxStoreResult.Created
          else accStoreResult)
    }

  def memoizedStoreDependencyInStore(sandbox: SandboxArtifact, depSandboxes: Set[SandboxArtifact]): SandBoxStoreResult = {
    val memoizedStore = getOrSetRootProjectProperty(
      StoreDependentSandboxes.RootProjectPropMemoizedStoreDependencyInStore,
      Memoize[(SandboxArtifact, Set[SandboxArtifact]), SandBoxStoreResult] {
        case (depSandbox, dependencies) => store.store(depSandbox, dependencies)
      }
    )

    memoizedStore(sandbox, depSandboxes)
  }


  def getOrSetRootProjectProperty[V <: Object](propertyName: String, propertyValue: => V): V = {
    val rootProject = getProject.getRootProject

    if (rootProject.hasProperty(propertyName)) {
      rootProject.getProperties.get(propertyName).asInstanceOf[V]
    }
    else {
      rootProject.getProperties.asInstanceOf[java.util.Map[String, Object]].put(propertyName, propertyValue)
      propertyValue
    }
  }
}

object StoreDependentSandboxes {
  val RootProjectPropSandboxStore = "haskell.cache.sandboxStore"
  val RootProjectPropMemoizedStoreDependencyInStore = "haskell.cache.memoizedStoreDependencyInStore"
}