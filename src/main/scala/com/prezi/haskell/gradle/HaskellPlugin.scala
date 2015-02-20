
package com.prezi.haskell.gradle

import org.gradle.api.{Plugin, Project}

class HaskellPlugin() extends Plugin[Project] {
  override def apply(project: Project): Unit = {
    project.getPlugins.apply("base")
    new HaskellProject(project)
  }
}