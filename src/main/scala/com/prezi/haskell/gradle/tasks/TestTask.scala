package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Executes cabal test with the proper sandbox chaining
 */
class TestTask extends DefaultTask with HaskellProjectSupport with HaskellDependencies with UsingHaskellTools {

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.cabalTest(getProject.getProjectDir, sandbox, dependentSandboxes)
  }
}
