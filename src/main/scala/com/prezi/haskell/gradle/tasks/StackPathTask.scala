package com.prezi.haskell.gradle.tasks

import java.io.{File, PrintWriter}

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import resource._

/**
 * Execute stack path and store its result
 */
class StackPathTask extends CabalExecTask {

  val outputFile = StackPathTask.getPathCache(getProject)

  dependsOn("generateStackYaml")
  getInputs.file(getProject.getProjectDir </> "stack.yaml")
  getOutputs.file(outputFile)

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    val output = tools.get.capturedStack(cabalContext().envConfigurer, getProject.getProjectDir, "path")
    for (writer <- managed(new PrintWriter(outputFile))) {
      writer.write(output)
    }
  }
}

object StackPathTask {
  def getPathCache(project: Project): File =
    project.getBuildDir </> "stack-path.out"
}