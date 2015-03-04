package com.prezi.haskell.gradle

import java.io.File

import com.prezi.haskell.gradle.tasks.GenerateGhcModCradle
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

class GhcModSupport(protected val project: Project) extends GhcModSupportImpl with ProjectExtender {

  addTasks
}

trait GhcModSupportImpl {
  this: ProjectExtender =>

  protected def addTasks(): Unit = {
    val genTask = project.getTasks.create("generateGhcModCradle", classOf[GenerateGhcModCradle])
    genTask.targetFile = Some(new File(project.getProjectDir, "ghc-mod.cradle"))
    genTask.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))

    val assembleTask = project.getTasks.getByPath(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)
    assembleTask.dependsOn(genTask)
  }
}