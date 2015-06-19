package com.prezi.haskell.gradle.extension.impl

import java.io.File

import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks._

trait SandboxSupportImpl {
  this: ProjectExtender =>

  protected def addSandboxInfoTask(): Unit = {
    createTask[SandboxInfo]("sandboxInfo")
  }

  protected def addSandboxDirectoriesTask(): Unit = {
    createTask[SandboxDirectories]("sandboxDirectories")
  }

  protected def addSandboxTask(): Unit = {
    val task = createTask[SandboxTask]("sandbox")
    task.tools = Some(getField[HaskellTools]("haskellTools"))
  }

  protected def addSandboxPackagesTask(): Unit = {
    val task = createTask[SandboxPackages]("sandboxPackages")
    task.tools = Some(getField[HaskellTools]("haskellTools"))
    task.configuration = Some(getConfiguration(Names.mainConfiguration))
  }

  protected def addStoreDependentSandboxesTask(): Unit = {
    val configTask = createTask[ConfigureSandboxTasks]("configureSandboxes")
    configTask.configuration = Some(getConfiguration(Names.mainConfiguration))

    val task = createTask[StoreDependentSandboxes]("storeDependentSandboxes")
    task.configuration = Some(getConfiguration(Names.mainConfiguration))

    configTask.storeTask = Some(task)
    task.getDependsOn.add(configTask)
  }

  protected def addCabalFreezeTask(): Unit = {
    val task = createTask[FreezeTask]("cabalFreeze")
    task.tools = Some(getField[HaskellTools]("haskellTools"))
    task.configuration = Some(getConfiguration(Names.mainConfiguration))
  }

  protected def sandFixDir : File

  protected def addCopySandFixTask(): Unit = {
    val task = createTask[CopySandFix]("copySandFix")
    task.sandFixDir = Some(sandFixDir)
  }
}
