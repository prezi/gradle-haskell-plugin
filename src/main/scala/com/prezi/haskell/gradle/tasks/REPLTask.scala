package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
  * Executes cabal repl or stack ghci
  */
class REPLTask extends StackExecTask {

  dependsOn("compileMain")

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    // TODO: check how scalaConsole task works in the gradle scala plugin
    tools.get.stack(stackRoot, Some(getProject.getProjectDir), "ghci")
  }
}
