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

    // TODO: check how scalaConsole task works in the gradle scala plugin
    if (haskellExtension.getUseStack) {
      tools.get.stack(stackRoot, cabalContext().envConfigurer, getProject.getProjectDir, "ghci")
    } else {
      tools.get.cabalREPL(cabalContext())
    }
  }
}
