package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal freeze
 */
class REPLTask extends CabalExecTask {

  dependsOn("compileMain")

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.cabalREPL(cabalContext())
  }
}
