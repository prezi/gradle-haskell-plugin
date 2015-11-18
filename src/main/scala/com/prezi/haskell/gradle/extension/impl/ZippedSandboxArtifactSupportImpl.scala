package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.extension.ProjectExtender
import com.prezi.haskell.gradle.tasks.ZippedSandbox

trait ZippedSandboxArtifactSupportImpl {
   this: ProjectExtender =>

   protected def defineZipSandboxArtifact(): Unit = {
     val zipSandbox = createTask[ZippedSandbox]("zipSandbox")
     project.getArtifacts.add("main", zipSandbox)
   }
 }
