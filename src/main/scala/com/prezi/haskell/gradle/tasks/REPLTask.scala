package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal repl or stack ghci
 */
class REPLTask extends CabalExecTask {

  dependsOn("compileMain")

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    if (haskellExtension.getUseStack) {
      tools.get.stack(cabalContext().envConfigurer, getProject.getProjectDir, "ghci")
    } else {
      tools.get.cabalREPL(cabalContext())
    }
  }
}
