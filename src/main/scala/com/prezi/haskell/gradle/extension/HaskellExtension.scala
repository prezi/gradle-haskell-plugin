package com.prezi.haskell.gradle.extension

import java.util

import com.prezi.haskell.gradle.extension.HaskellExtension.PropertyKey
import com.prezi.haskell.gradle.external.HaskellTools.{Cabal122, CabalVersion}
import com.prezi.haskell.gradle.external.ToolsBase.{EnvConfigurer, OptEnvConfigurer}
import com.prezi.haskell.gradle.incubating.{DefaultProjectSourceSet, ProjectSourceSet}
import org.gradle.api.{Action, Project}
import org.gradle.internal.reflect.Instantiator

/**
 * Project extension holding the source set and other properties for Haskell projects
 * @param instantiator Gradle object instantiator
 */
class HaskellExtension(instantiator: Instantiator, project: Project) extends java.io.Serializable {
  private val sources_ : ProjectSourceSet = instantiator.newInstance(classOf[DefaultProjectSourceSet], instantiator)
  private var profiling_ : Boolean = !project.hasProperty(PropertyKey.GhcDisableProfiling)
  private var useStack_ : Boolean = project.hasProperty(PropertyKey.UseStack)
  private var ghcVersion_ : String = "ghc-7.10.2"
  private var snapshotId_ : String = "lts-3.13"
  private var cabalVersion_ : CabalVersion = Cabal122
  private var cabalConfigFile_ : Option[String] =
    if (project.hasProperty(PropertyKey.CabalConfigFile)) Some(project.getProperties.get(PropertyKey.CabalConfigFile).toString)
    else None
  private var packageFlags_ : java.util.Map[String, java.util.Map[String, String]] =
    new util.HashMap[String, java.util.Map[String, String]]()
  private var isExecutable_ : Boolean = false
  private var envConfigurer_ : OptEnvConfigurer = None

  def getSources = sources_

  def sources(action: Action[ProjectSourceSet]): Unit = {
    action.execute(sources_)
  }

  def getProfiling = profiling_
  def setProfiling(value: Boolean): Unit = {
    profiling_ = value
  }
  def profiling(value: Boolean): Unit = setProfiling(value)

  def getUseStack = useStack_
  def setUseStack(value: Boolean): Unit = {
    useStack_ = value
  }
  def useStack(value: Boolean): Unit = setUseStack(value)

  def getCabalConfigFile = cabalConfigFile_
  def setCabalConfigFile(value: String): Unit = {
    cabalConfigFile_ = Some(value)
  }
  def cabalConfigFile(value: String): Unit = setCabalConfigFile(value)

  def getEnvConfigurer = envConfigurer_
  def setEnvConfigurer(value: OptEnvConfigurer): Unit = {
    envConfigurer_ = value
  }
  def envConfigurer(value: EnvConfigurer): Unit = setEnvConfigurer(Some(value))

  def cabalType: CabalVersion = cabalVersion_
  def getCabalVersion: String = cabalVersion_.toString
  def setCabalVersion(value: String): Unit = {
    cabalVersion_ = CabalVersion.parse(value)
  }
  def cabalVersion(value: String): Unit = setCabalVersion(value)

  def ghcVersion: String = ghcVersion_
  def getGhcVersion: String = ghcVersion_
  def setGhcVersion(value: String): Unit = {
    ghcVersion_ = value
  }
  def ghcVersion(value: String): Unit = setGhcVersion(value)

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
}

object HaskellExtension {
  object PropertyKey {
    val GhcDisableProfiling = "ghc-disable-profiling"
    val CabalConfigFile = "cabal-config-file"
    val UseStack = "use-stack"
  }
}