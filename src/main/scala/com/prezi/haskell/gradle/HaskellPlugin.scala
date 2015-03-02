
package com.prezi.haskell.gradle

import javax.inject.Inject

import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.{Plugin, Project}
import org.gradle.internal.reflect.Instantiator

class HaskellPlugin @Inject() (instantiator: Instantiator, fileResolver: FileResolver) extends Plugin[Project] {
  override def apply(project: Project): Unit = {
    project.getPlugins.apply(classOf[BasePlugin])

    new HaskellProject(project, instantiator, fileResolver)
  }
}