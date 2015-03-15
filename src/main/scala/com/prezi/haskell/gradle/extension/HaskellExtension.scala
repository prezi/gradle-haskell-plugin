package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.HaskellExtension.PropertyKey
import com.prezi.haskell.gradle.external.HaskellTools.{EnvConfigurer, OptEnvConfigurer}
import groovy.lang.Closure
import org.gradle.api.{Action, Project}
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.internal.DefaultProjectSourceSet

/**
 * Project extension holding the source set and other properties for Haskell projects
 * @param instantiator Gradle object instantiator
 */
class HaskellExtension(instantiator: Instantiator, project: Project) extends java.io.Serializable {
  private val sources_ : ProjectSourceSet = instantiator.newInstance(classOf[DefaultProjectSourceSet], instantiator)
  private var profiling_ : Boolean = !project.hasProperty(PropertyKey.GhcDisableProfiling)
  private var cabalConfigFile_ : Option[String] =
    if (project.hasProperty(PropertyKey.CabalConfigFile)) Some(project.getProperties.get(PropertyKey.CabalConfigFile).toString)
    else None
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
}

object HaskellExtension {
  object PropertyKey {
    val GhcDisableProfiling = "ghc-disable-profiling"
    val CabalConfigFile = "cabal-config-file"
  }
}