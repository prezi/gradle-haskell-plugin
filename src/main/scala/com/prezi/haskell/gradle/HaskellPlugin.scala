
package com.prezi.haskell.gradle

import javax.inject.Inject

import com.prezi.haskell.gradle.extension.HaskellProject
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.{Plugin, Project}
import org.gradle.internal.reflect.Instantiator

/**
 * Main entry point of the gradle-haskell-plugin
 * @param instantiator Gradle's object instantiator
 * @param fileResolver Gradle's file resolver
 */
class HaskellPlugin @Inject() (instantiator: Instantiator, fileResolver: FileResolver) extends Plugin[Project] {
  override def apply(project: Project): Unit = {
    project.getPlugins.apply(classOf[BasePlugin])

    new HaskellProject(project, instantiator, fileResolver)
  }
}