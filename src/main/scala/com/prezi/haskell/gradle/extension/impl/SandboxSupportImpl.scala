package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks._

trait SandboxSupportImpl {
   this : ProjectExtender =>

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

   protected def addExtractDependentSandboxesTask(): Unit = {
     val configTask = createTask[ConfigureExtractDependentSandboxes]("configureExtractDependentSandboxes")
     configTask.configuration = Some(getConfiguration(Names.mainConfiguration))

     val task = createTask[ExtractDependentSandboxes]("extractDependentSandboxes")
     task.configuration = Some(getConfiguration(Names.mainConfiguration))

     configTask.extractTask = Some(task)
     task.getDependsOn.add(configTask)
   }
 }
