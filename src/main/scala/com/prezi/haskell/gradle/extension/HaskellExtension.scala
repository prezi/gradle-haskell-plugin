package com.prezi.haskell.gradle.extension

import java.util

import com.prezi.haskell.gradle.extension.HaskellExtension.PropertyKey
import com.prezi.haskell.gradle.incubating.{DefaultProjectSourceSet, ProjectSourceSet}
import com.prezi.haskell.gradle.model.{GHC801, GHC801WithSierraFix, GHCVersion}
import org.gradle.api.{Action, GradleException, Project}
import org.gradle.internal.reflect.Instantiator

/**
 * Project extension holding the source set and other properties for Haskell projects
  *
  * @param instantiator Gradle object instantiator
 */
class HaskellExtension(instantiator: Instantiator, project: Project) extends java.io.Serializable {
  private val sources_ : ProjectSourceSet = instantiator.newInstance(classOf[DefaultProjectSourceSet], instantiator)
  private var profiling_ : Boolean = !project.hasProperty(PropertyKey.GhcDisableProfiling)
  private var ghcVersion_ : String = "ghc-8.0.1"
  private var snapshotId_ : String = "lts-7.14"
  private var packageFlags_ : java.util.Map[String, java.util.Map[String, String]] =
    new util.HashMap[String, java.util.Map[String, String]]()
  private var isExecutable_ : Boolean = false
  private var overriddenSnapshotVersionsCacheDir_ : Option[String] =
    if (project.hasProperty(PropertyKey.SnapshotVersionsCacheDir)) Some(project.getProperties.get(PropertyKey.SnapshotVersionsCacheDir).toString)
    else None
  private var stackRoot_ : Option[String] =
    if (project.hasProperty(PropertyKey.StackRoot)) Some(project.getProperties.get(PropertyKey.StackRoot).toString)
    else None

  def getSources = sources_

  def sources(action: Action[ProjectSourceSet]): Unit = {
    action.execute(sources_)
  }

  def getProfiling = profiling_
  def setProfiling(value: Boolean): Unit = {
    profiling_ = value
  }
  def profiling(value: Boolean): Unit = setProfiling(value)

  def ghcVersion: String = ghcVersion_
  def getGhcVersion: String = ghcVersion_
  def setGhcVersion(value: String): Unit = {
    ghcVersion_ = value
  }
  def ghcVersion(value: String): Unit = setGhcVersion(value)

  def parsedGHCVersion: GHCVersion = {
    ghcVersion_ match {
      case "ghc-8.0.1" if System.getProperty("os.name") == "Mac OS X" =>
        GHC801WithSierraFix
      case "ghc-8.0.1" =>
        GHC801
      case _ =>
        throw new GradleException(s"Unsupported ghc version: $ghcVersion_")
    }
  }

  def snapshotId: String = snapshotId_
  def getSnapshotId: String = snapshotId_
  def setSnapshotId(value: String): Unit = {
    snapshotId_ = value
  }
  def snapshotId(value: String): Unit = setSnapshotId(value)

  def packageFlags = packageFlags_
  def getPackageFlags = packageFlags_
  def setPackageFlags(value: java.util.Map[String, java.util.Map[String, String]]): Unit = {
    packageFlags_ = value
  }

  def isExecutable = isExecutable_
  def getIsExecutable = isExecutable_
  def setIsExecutable(value: Boolean): Unit = {
    isExecutable_ = value
  }

  def overriddenSnapshotVersionsCacheDir  = overriddenSnapshotVersionsCacheDir_
  def getOverriddenSnapshotVersionsCacheDir = overriddenSnapshotVersionsCacheDir_
  def setOverriddenSnapshotVersionsCacheDir(value: String) = {
    overriddenSnapshotVersionsCacheDir_ = Some(value)
  }

  def stackRoot  = stackRoot_
  def getStackRoot = stackRoot_
  def setStackRoot(value: String) = {
    stackRoot_ = Some(value)
  }
}

object HaskellExtension {
  object PropertyKey {
    val GhcDisableProfiling = "ghc-disable-profiling"
    val SnapshotVersionsCacheDir = "snapshot-versions-dir"
    val StackRoot = "stack-root"
  }
}