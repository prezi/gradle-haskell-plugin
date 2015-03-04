package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks.{CopySandFix, FixDependentSandboxes}

trait SandFixSupportImpl {
   this : ProjectExtender =>

   private val sandFixDir = project.getBuildDir </> "sandfix"

   protected def addCopySandFixTask(): Unit = {
     val task = createTask[CopySandFix]("copySandFix")
     task.sandFixDir = Some(sandFixDir)
   }

   protected def addFixDependentSandboxesTask(): Unit = {
     val task = createTask[FixDependentSandboxes]("fixDependentSandboxes")
     task.configuration = Some(getConfiguration(Names.mainConfiguration))
     task.tools = Some(getField[HaskellTools]("haskellTools"))
     task.sandFixPath = Some(sandFixDir)
   }
 }
