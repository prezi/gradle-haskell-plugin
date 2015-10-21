package com.prezi.haskell.gradle.tasks

import java.io.{File, PrintWriter}

import com.google.common.io.Files
import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.{DefaultTask, Project}
import org.gradle.api.tasks.TaskAction
import resource._

/**
 * Execute stack path and store its result
 */
class StackPathTask extends DefaultTask
  with HaskellProjectSupport
  with UsingHaskellTools {

  val outputFile = StackPathTask.getPathCache(getProject)

  dependsOn("generateStackYaml")
  getInputs.file(getProject.getProjectDir </> "stack.yaml")
  getOutputs.file(outputFile)

  @TaskAction
  def run(): Unit = {
    needsToolsSet

    val output = tools.get.capturedStack(haskellExtension.getEnvConfigurer, getProject.getProjectDir, "path")
    Files.createParentDirs(outputFile)
    for (writer <- managed(new PrintWriter(outputFile))) {
      writer.write(output)
    }
  }
}

object StackPathTask {
  def getPathCache(project: Project): File =
    project.getBuildDir </> "stack-path.out"
}