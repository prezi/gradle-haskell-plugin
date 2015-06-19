package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal freeze
 */
class FreezeTask extends CabalExecTask {

  dependsOn("sandbox")
  dependsOn("storeDependentSandboxes")

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.cabalFreeze(cabalContext())
  }
}
