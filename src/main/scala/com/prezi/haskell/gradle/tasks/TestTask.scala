package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal test with the proper sandbox chaining
 */
class TestTask extends CabalExecTask {

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.cabalTest(cabalContext())
  }
}
