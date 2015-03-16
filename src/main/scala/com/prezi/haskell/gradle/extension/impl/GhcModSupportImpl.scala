package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.tasks.GenerateGhcModCradle
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.language.base.plugins.LifecycleBasePlugin._

trait GhcModSupportImpl {
   this: ProjectExtender =>

   protected def addTasks(): Unit = {
     val genTask = createTask[GenerateGhcModCradle]("generateGhcModCradle")
     genTask.targetFile = Some(project.getProjectDir </> "ghc-mod.cradle")
     genTask.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))

     val assembleTask = getTask[Task](ASSEMBLE_TASK_NAME)
     assembleTask.dependsOn(genTask)
   }

  protected def extendCleanTask(): Unit = {
    val cleanTask = getTask[Delete](CLEAN_TASK_NAME)
    cleanTask.delete(project.getProjectDir </> "ghc-mod.cradle")
  }
 }
