package com.prezi.haskell.gradle.tasks

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Generates the ghc-mod.cradle file containing all the package databases ghc-mod needs to
 * work on the given project.
 */
class GenerateGhcModCradle extends DefaultTask with HaskellProjectSupport with HaskellDependencies {

  private var targetFile_ : Option[File] = None

  def targetFile = targetFile_
  def targetFile_=(value: Option[File]): Unit = {
    targetFile_ = value

    if (value.isDefined) {
      getOutputs.file(value.get)
    }
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet

    if (!targetFile.isDefined) {
      throw new IllegalStateException("targetFile is not specified")
    }

    val cradleFile = generateContent(dependentSandboxes ::: List(sandbox))
    Files.write(targetFile.get.toPath, cradleFile.getBytes(StandardCharsets.UTF_8))
  }

  private def generateContent(sandboxes: List[Sandbox]): String = {
    val content = new StringBuilder()

    for (sandbox <- sandboxes) {
      content.append(sandbox.packageDb.getAbsolutePath)
      content.append('\n')
    }

    content.mkString
  }
}
