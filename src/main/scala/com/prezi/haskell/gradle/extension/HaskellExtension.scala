package com.prezi.haskell.gradle.extension

import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.internal.DefaultProjectSourceSet

/**
 * Project extension holding the source set for Haskell projects
 * @param instantiator Gradle object instantiator
 */
class HaskellExtension(instantiator: Instantiator) extends java.io.Serializable {

  private val sources_ : ProjectSourceSet = instantiator.newInstance(classOf[DefaultProjectSourceSet], instantiator)

  def getSources = sources_

  def sources(action: Action[ProjectSourceSet]): Unit = {
    action.execute(sources_)
  }
}
