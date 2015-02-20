package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Initializes the sandbox of a project
 */
class SandboxTask extends DefaultTask {
  private val sandbox = getProject.getExtensions.getByType(classOf[Sandbox])
  var tools: Option[HaskellTools] = None

  dependsOn("sandboxDirectories")
  getOutputs.file(new File(sandbox.packageDb, "package.cache"))

  @TaskAction
  def run(): Unit = {
    if (!tools.isDefined) {
      throw new IllegalStateException("tools is not specified")
    }

    tools.get.ghcPkgRecache(sandbox)
  }
}
