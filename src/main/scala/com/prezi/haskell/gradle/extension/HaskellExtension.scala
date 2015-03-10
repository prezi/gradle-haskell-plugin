package com.prezi.haskell.gradle.extension

import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.internal.DefaultProjectSourceSet

/**
 * Project extension holding the source set and other properties for Haskell projects
 * @param instantiator Gradle object instantiator
 */
class HaskellExtension(instantiator: Instantiator) extends java.io.Serializable {

  private val sources_ : ProjectSourceSet = instantiator.newInstance(classOf[DefaultProjectSourceSet], instantiator)
  private var profiling_ : Boolean = true

  def getSources = sources_

  def sources(action: Action[ProjectSourceSet]): Unit = {
    action.execute(sources_)
  }

  def getProfiling = profiling_
  def setProfiling(value: Boolean): Unit = {
    profiling_ = value
  }
  def profiling(value: Boolean): Unit = setProfiling(value)
}
