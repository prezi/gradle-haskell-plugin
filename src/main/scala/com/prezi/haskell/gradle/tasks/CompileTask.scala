package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.FunctionalSourceSet

/**
 * Executes cabal install with the proper sandbox chaining
 */
class CompileTask extends DefaultTask with HaskellProjectSupport with HaskellDependencies with UsingHaskellTools {
  val buildDir = new File(getProject.getProjectDir, "dist")

  dependsOn("sandbox")
  dependsOn("fixDependentSandboxes")

  def attachToSourceSet(sourceSet: FunctionalSourceSet) = {
    getDependsOn.add(sourceSet)
    getOutputs.dir(getProject.getExtensions.getByType(classOf[Sandbox]).root)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    tools.get.cabalInstall(getProject.getProjectDir, sandbox, dependentSandboxes)
  }
}
