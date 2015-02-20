package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Prints basic info about a project's sandbox
 */
class SandboxInfo extends DefaultTask {

  @TaskAction
  def run(): Unit = {

    val sandbox = getProject.getExtensions.getByType(classOf[Sandbox])

    println(s"Project ${getProject.getName} GHC sandbox info")
    println()
    println(s"Package db: ${sandbox.packageDb}")
    println(s"Install location: ${sandbox.installPrefix}")
  }
}
