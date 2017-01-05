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

  val outputFile: File = StackPathTask.getPathCache(getProject)

  dependsOn("generateStackYaml")
  getInputs.file(getProject.getProjectDir </> "stack.yaml")
  getOutputs.file(outputFile)

  @TaskAction
  def run(): Unit = {
    needsToolsSet

    tools.get.stack(haskellExtension.stackRoot, haskellExtension.getEnvConfigurer, getProject.getProjectDir, "setup")

    val output = tools.get.capturedStack(haskellExtension.getStackRoot, haskellExtension.getEnvConfigurer, getProject.getProjectDir, "path")
    Files.createParentDirs(outputFile)
    for (writer <- managed(new PrintWriter(outputFile))) {
      writer.write(output)
    }
  }
}

class StackBinPathTask extends DefaultTask
  with HaskellProjectSupport
  with UsingHaskellTools {

  val outputFile: File = StackPathTask.getBinPathCache(getProject)

  getInputs.file(getProject.getProjectDir </> "stack.yaml")
  getOutputs.file(outputFile)

  @TaskAction
  def run(): Unit = {
    needsToolsSet

    tools.get.stack(haskellExtension.stackRoot, haskellExtension.getEnvConfigurer, getProject.getProjectDir, "setup")

    val output = tools.get.capturedStack(haskellExtension.getStackRoot, haskellExtension.getEnvConfigurer, getProject.getProjectDir, "path")
    Files.createParentDirs(outputFile)
    for (writer <- managed(new PrintWriter(outputFile))) {
      writer.write(output)
    }
  }
}

object StackPathTask {
  def getPathCache(project: Project): File =
    project.getBuildDir </> "stack-path.out"
  def getBinPathCache(project: Project): File =
    project.getBuildDir </> "stack-bin-path.out"
}