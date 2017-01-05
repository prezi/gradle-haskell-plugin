package com.prezi.haskell.gradle.tasks

import java.io.File
import java.util

import com.prezi.haskell.gradle.Profiling.measureTime
import com.prezi.haskell.gradle.model.SandboxArtifact
import com.prezi.haskell.gradle.model.sandboxstore.SandBoxStoreResult
import com.prezi.haskell.gradle.util.FileLock
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.{Configuration, ResolvedDependency}
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._
import scala.collection.mutable

class StoreDependentSandboxes
  extends DefaultTask
  with HaskellDependencies
  with UsingHaskellTools
  with HaskellProjectSupport
  with TaskLogging {

  dependsOn("copySandFix")

  var sandFixPath: Option[File] = None

  private var isAnySandboxUpdated_ : Boolean = false
  def isAnySandboxUpdated: Boolean = isAnySandboxUpdated_
  private def isAnySandboxUpdated_=(value: Boolean): Unit = {
    isAnySandboxUpdated_ = value
    debug(s"isAnySandboxUpdated: ${isAnySandboxUpdated_}")
  }

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    val fileLock = new FileLock(new File(getProject.getRootProject.getBuildDir, "store-sandbox.lock"))
    fileLock.lock()
    try {
      needsConfigurationSet

      info(s"Storing dependent sandboxes for ${getProject.getName}")
      val storeDependencyResults =
        for (dependency <- configuration.get.getResolvedConfiguration.getFirstLevelModuleDependencies.asScala) yield {
          storeDependency(dependency)
        }

      dumpSandboxStoreResults(storeDependencyResults)
      isAnySandboxUpdated = storeDependencyResults.exists {
        _._2 == SandBoxStoreResult.Created
      }
    } finally {
      fileLock.release()
    }
  }

  def storeDependency(dependency: ResolvedDependency, prefix: String = ""): (Set[SandboxArtifact], SandBoxStoreResult) = {
    info(s"${prefix}Dependency ${dependency.getName}")

    // Storing child dependencies
    val (depSandboxes: Set[SandboxArtifact], aggregatedStoreResult: SandBoxStoreResult) =
      foldStoreDependencyResults(
        (for {
          child <- dependency.getChildren.asScala
          storeDependencyResult <- List(storeDependency(child, prefix + "  "))
        } yield storeDependencyResult).toSet
      )

    // Collecting all the dependent sandbox artifacts
    val (sandboxes, dt1) = measureTime {
      dependency.getAllModuleArtifacts
        .asScala
        .map(artifact => new SandboxArtifact(artifact.getName, artifact.getFile))
        .toSet
    }
    info(s"${prefix}[PERFORMANCE] Collecting dependent sandbox artifacts for ${dependency.getName} took $dt1 seconds")

    // Storing the sandbox and its dependent sandboxes
    val (sandboxStoreResults, dt2) = measureTime {
      for (sandbox <- sandboxes) yield {
        val res = memoizedStoreDependencyInStore(sandbox, depSandboxes)
        debug(s"$res <- memoizedStoreDependencyInStore($sandbox, $depSandboxes)")
        res
      }
    }
    info(s"${prefix}[PERFORMANCE] Storing sandbox and its dependent sandboxes for ${dependency.getName} took $dt2 seconds")

    (sandboxes, sandboxStoreResults.foldLeft(aggregatedStoreResult)(aggregateStoreResult))
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
        val (res, dt) = measureTime { store.store(haskellExtension.stackRoot, sandbox, depSandboxes) }
        info(s"[PERFORMANCE] Storing and fixing sandbox $sandbox took $dt seconds")
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

      debug(s"Setting root project property: [key = $propertyName, value = $value]")

      rootProject.getExtensions.add(propertyName, value)
      value
    }
  }

  private def aggregateStoreResult(aggregatedStoreResult: SandBoxStoreResult, newResult: SandBoxStoreResult): SandBoxStoreResult = {
    if (aggregatedStoreResult == SandBoxStoreResult.Created || newResult == SandBoxStoreResult.Created) {
      SandBoxStoreResult.Created
    } else {
      SandBoxStoreResult.AlreadyExists
    }
  }

  private def dumpSandboxStoreResults(storeDependencyResults: mutable.Set[(Set[SandboxArtifact], SandBoxStoreResult)]): Unit = {
    if (getLogger.isDebugEnabled) {
      getLogger.log(LogLevel.DEBUG, "StoreDependencyResults:")
      storeDependencyResults foreach { case (sandboxArtifacts, storeResult) =>
        debug(s"$sandboxArtifacts: $storeResult")
      }
    }
  }
}

object StoreDependentSandboxes {
  val RootProjectPropSandboxStore = "haskell.cache.sandboxStore"
  val RootProjectPropSandboxStoreResult = "haskell.cache.sandboxStoreResult"
}