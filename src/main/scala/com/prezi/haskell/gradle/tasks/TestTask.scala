package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
  * Executes cabal/stack test with the proper sandbox chaining
  */
class TestTask extends StackExecTask {

  dependsOn("generateStackYaml")

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    val profilingArgs = if (useProfiling) {
      List("--executable-profiling", "--library-profiling")
    } else {
      List()
    }
    tools.get.stack(stackRoot, Some(getProject.getProjectDir), "test" :: profilingArgs: _*)
  }
}
