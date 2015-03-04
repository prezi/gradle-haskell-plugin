package com.prezi.haskell.gradle.extension.impl

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks.{CopySandFix, FixDependentSandboxes}
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

trait SandFixSupportImpl {
   this : ProjectExtender =>

   protected val sandFixConfig = addConfiguration
   private val sandFixDir = project.getBuildDir </> "sandfix"

   private def addConfiguration(): Configuration = {
     val sandfixConfig = addConfiguration(Names.sandFixConfiguration)
     sandfixConfig.setVisible(false)

     sandfixConfig.getDependencies.add(
       new DefaultExternalModuleDependency("com.prezi.engine.dom", "pdom-sandfix", "1.+", "haskellSrc")
     )

     sandfixConfig
   }

   protected def addCopySandFixTask(): Unit = {
     val task = createTask[CopySandFix]("copySandFix")
     task.setUp(sandFixConfig, sandFixDir)
   }

   protected def addFixDependentSandboxesTask(): Unit = {
     val task = createTask[FixDependentSandboxes]("fixDependentSandboxes")
     task.configuration = Some(getConfiguration(Names.mainConfiguration))
     task.tools = Some(getField[HaskellTools]("haskellTools"))
     task.sandFixPath = Some(new File(sandFixDir, "sandfix-1.1"))
   }
 }
