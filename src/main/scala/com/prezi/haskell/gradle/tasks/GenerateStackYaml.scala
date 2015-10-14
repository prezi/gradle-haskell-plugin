package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.model.Sandbox
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateStackYaml extends DefaultTask with HaskellProjectSupport with HaskellDependencies {

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

    val yamlFile = generateContent(dependentSandboxes)
    FileUtils.writeStringToFile(targetFile.get, yamlFile)
  }

  private def generateContent(sandboxes: List[Sandbox]): String = {
    val content = new StringBuilder()
    val resolver = s"ghc-${haskellExtension.ghcVersion}"

    content.append(
      s"""flags: {}
        | packages:
        |   - .
        | resolver: $resolver""".stripMargin)

    content.append("\n\nextra-package-dbs:")

    if (sandboxes.isEmpty) {
      content.append(" []\n")
    } else {
      content.append('\n')
      for (sandbox <- sandboxes) {
        content.append("  - ")
        content.append(sandbox.packageDb.getAbsolutePath)
        content.append('\n')
      }
    }

    content.append("extra-deps: []\n") // TODO: use snapshot-versions
    val snapshotId = haskellExtension.snapshotId

    content.mkString
  }
}

