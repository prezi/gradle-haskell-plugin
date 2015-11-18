package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.tasks.{ZippedExecutables, ZippedSandbox}

trait ZippedSandboxArtifactSupportImpl {
   this: ProjectExtender =>

   protected def defineZipSandboxArtifact(): Unit = {
     val zipSandbox = createTask[ZippedSandbox]("zipSandbox")
     project.getArtifacts.add("main", zipSandbox)
   }

   protected def defineZipExecutableArtifact(): Unit = {
     val zippedExecutables = createTask[ZippedExecutables]("zipExecutables")
     project.getArtifacts.add("main", zippedExecutables)
   }
 }
