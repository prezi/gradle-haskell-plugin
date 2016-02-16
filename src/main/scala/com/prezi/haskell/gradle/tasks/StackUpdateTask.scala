package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes stack update
 */
class StackUpdateTask extends CabalExecTask {

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.stack(stackRoot, cabalContext().envConfigurer, getProject.getProjectDir, "update")
  }
}
