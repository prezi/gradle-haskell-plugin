package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal/stack test with the proper sandbox chaining
 */
class TestTask extends CabalExecTask {

  if (haskellExtension.getUseStack) {
    dependsOn("generateStackYaml")
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    if (haskellExtension.getUseStack) {
      val profilingArgs = if (cabalContext().profiling) {
        List("--executable-profiling", "--library-profiling")
      } else {
        List()
      }
      tools.get.stack(stackRoot, cabalContext().envConfigurer, Some(getProject.getProjectDir), "test" :: profilingArgs : _*)
    } else {
      tools.get.cabalTest(cabalContext())
    }
  }
}
