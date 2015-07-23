package com.prezi.haskell.gradle.tasks

import java.io.File
import java.util

import com.prezi.haskell.gradle.model.{SandBoxStoreResult, SandboxArtifact}
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{Configuration, ResolvedDependency}
import org.gradle.api.logging.LogLevel
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

    if (getLogger.isDebugEnabled) {
      getLogger.log(LogLevel.DEBUG, "StoreDependencyResults:")
      storeDependencyResults foreach { case (sandboxArtifacts, storeResult) =>
        getLogger.debug("{}: {}", sandboxArtifacts, storeResult)
      }
    }

    _isAnySandboxUpdated = storeDependencyResults.exists { _._2 == SandBoxStoreResult.Created }

    getLogger.debug("isAnySandboxUpdated: {}", isAnySandboxUpdated)
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
        val res = memoizedStoreDependencyInStore(sandbox, depSandboxes)
        getLogger.debug("{} <- memoizedStoreDependencyInStore({}, {})", res, sandbox, depSandboxes)
        res
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
    synchronized {
      val sandboxStoreCache: util.Map[String, String] =
        getOrSetRootProjectProperty[util.Map[String, String]](
          StoreDependentSandboxes.RootProjectPropSandboxStoreResult,
          new util.HashMap[String, String]() // this needs to be a java (not scala) collection, otherwise classloader related cast issues will happen
        )

      val key = sandbox.toNormalizedString
      if (sandboxStoreCache.containsKey(key)) {
        SandBoxStoreResult(sandboxStoreCache.get(key))
      } else {
        val res = store.store(sandbox, depSandboxes)
        sandboxStoreCache.put(key, res.toNormalizedString)
        res
      }
    }
  }


  def getOrSetRootProjectProperty[V <: Object](propertyName: String, propertyValue: => V): V = {
    val rootProject = getProject.getRootProject

    if (rootProject.hasProperty(propertyName)) {
      rootProject.getProperties.get(propertyName).asInstanceOf[V]
    } else {
      val value = propertyValue
      getLogger.debug("Setting root project property: [key = {}, value = {}]", propertyName, value)
      rootProject.getExtensions.add(propertyName, value)
      value
    }
  }
}

object StoreDependentSandboxes {
  val RootProjectPropSandboxStore = "haskell.cache.sandboxStore"
  val RootProjectPropSandboxStoreResult = "haskell.cache.sandboxStoreResult"
}