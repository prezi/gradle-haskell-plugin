package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.external.{Git, HaskellTools}
import com.prezi.haskell.gradle.tasks.{StackPathTask, GenerateStackYaml}
import org.gradle.api.tasks.Delete
import org.gradle.language.base.plugins.LifecycleBasePlugin._

trait StackSupportImpl {
  this: ProjectExtender =>

  protected def addTasks(): Unit = {
    val genTask = createTask[GenerateStackYaml]("generateStackYaml")
    genTask.targetFile = Some(project.getProjectDir </> "stack.yaml")
    genTask.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))
    genTask.tools = Some(getField[HaskellTools]("haskellTools"))
    genTask.git = Some(getField[Git]("git"))

    val pathTask = createTask[StackPathTask]("stackPath")
    pathTask.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))
    pathTask.tools = Some(getField[HaskellTools]("haskellTools"))
  }

  protected def extendCleanTask(): Unit = {
    val cleanTask = getTask[Delete](CLEAN_TASK_NAME)
    cleanTask.delete(project.getProjectDir </> "stack.yaml")
    cleanTask.delete(project.getProjectDir </> ".stack-work")
  }

}
