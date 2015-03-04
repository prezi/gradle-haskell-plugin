package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.tasks.GenerateGhcModCradle
import org.gradle.api.Task
import org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME

trait GhcModSupportImpl {
   this: ProjectExtender =>

   protected def addTasks(): Unit = {
     val genTask = createTask[GenerateGhcModCradle]("generateGhcModCradle")
     genTask.targetFile = Some(project.getProjectDir </> "ghc-mod.cradle")
     genTask.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))

     val assembleTask = getTask[Task](ASSEMBLE_TASK_NAME)
     assembleTask.dependsOn(genTask)
   }
 }
