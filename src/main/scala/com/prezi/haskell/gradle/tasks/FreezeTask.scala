package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal freeze
 */
class FreezeTask extends CabalExecTask {

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.cabalConfigure(cabalContext())
    tools.get.cabalFreeze(cabalContext())
  }
}
