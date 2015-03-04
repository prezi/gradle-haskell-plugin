package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Lists the packages in a project's sandbox, and all its dependent sandboxes
 */
class SandboxPackages extends DefaultTask with HaskellProjectSupport with HaskellDependencies with UsingHaskellTools {
   dependsOn("sandbox")

   @TaskAction
   def run(): Unit = {
     needsToolsSet
     needsConfigurationSet

     tools.get.ghcPkgList(dependentSandboxes.+:(sandbox))
   }
 }
