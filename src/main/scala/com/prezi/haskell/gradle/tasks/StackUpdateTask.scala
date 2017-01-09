package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes stack update
 */
class StackUpdateTask extends StackExecTask {

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.stack(stackRoot, None, "update")
  }
}
