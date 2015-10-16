package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Prints basic info about a project's sandbox
 */
class SandboxInfo extends DefaultTask with HaskellProjectSupport with UsesSandbox {

  @TaskAction
  def run(): Unit = {
    println(s"Project ${getProject.getName} GHC sandbox info")
    println()
    println(s"Package db: ${sandbox.packageDb}")
    println(s"Install location: ${sandbox.installPrefix}")
  }
}
