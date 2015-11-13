package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Creates the sandbox directory structure for a project
 */
class SandboxDirectories
  extends DefaultTask
  with HaskellProjectSupport
  with UsesSandbox
  with TaskLogging {

  getOutputs.dir(sandbox.packageDb)
  getOutputs.dir(sandbox.installPrefix)

  @TaskAction
  def run(): Unit = {

    debug(s"Creating directory ${sandbox.packageDb}")
    sandbox.packageDb.mkdirs()

    debug(s"Creating directory ${sandbox.installPrefix}")
    sandbox.installPrefix.mkdirs()
  }
}
