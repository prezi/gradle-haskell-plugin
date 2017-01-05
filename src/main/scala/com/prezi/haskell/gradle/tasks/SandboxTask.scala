package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Initializes the sandbox of a project
 */
class SandboxTask extends DefaultTask
  with HaskellProjectSupport
  with UsingHaskellTools
  with UsesSandbox {

  dependsOn("sandboxDirectories")

  @TaskAction
  def run(): Unit = {
    needsToolsSet

    if (!(sandbox.packageDb </> "package.cache").exists()) {
      tools.get.ghcPkgRecache(haskellExtension.getEnvConfigurer, ghcPkgPath(getProject), sandbox)
    }
  }
}
