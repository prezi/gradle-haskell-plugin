package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.tasks.GenerateStackYaml
import org.gradle.api.tasks.Delete
import org.gradle.language.base.plugins.LifecycleBasePlugin._

trait StackSupportImpl {
  this: ProjectExtender =>

  protected def addTasks(): Unit = {
    val genTask = createTask[GenerateStackYaml]("generateStackYaml")
    genTask.targetFile = Some(project.getProjectDir </> "stack.yaml")
    genTask.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))
  }

  protected def extendCleanTask(): Unit = {
    val cleanTask = getTask[Delete](CLEAN_TASK_NAME)
    cleanTask.delete(project.getProjectDir </> "stack.yaml")
    cleanTask.delete(project.getProjectDir </> ".stack-work")
  }

}
