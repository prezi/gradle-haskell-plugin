package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal/stack test with the proper sandbox chaining
 */
class TestTask extends CabalExecTask {

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    if (haskellExtension.getUseStack) {
      tools.get.stack(cabalContext().envConfigurer, getProject.getProjectDir, "test")
    } else {
      tools.get.cabalTest(cabalContext())
    }
  }
}
