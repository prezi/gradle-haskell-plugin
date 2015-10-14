package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.external.SnapshotVersions
import com.prezi.haskell.gradle.model.Sandbox
import org.apache.commons.io.FileUtils
import org.gradle.api.{GradleException, DefaultTask}
import org.gradle.api.tasks.TaskAction

class GenerateStackYaml
  extends DefaultTask
  with HaskellProjectSupport
  with HaskellDependencies
  with UsingHaskellTools
  with UsingGit {

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
    needsGitSet
    needsToolsSet

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
         |packages:
         |  - .
         |resolver: $resolver""".stripMargin)

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

    val snapshotVersions = new SnapshotVersions(haskellExtension.getEnvConfigurer, getProject.exec, tools.get, git.get)
    val deps = snapshotVersions.run(haskellExtension.snapshotId, findCabalFile())

    if (deps.length > 0) {
      content.append("extra-deps:\n")
      for (dep <- deps) {
        content.append("  - ")
        content.append(dep)
        content.append('\n')
      }
    } else {
      content.append("extra-deps: []\n")
    }

    content.mkString
  }

  private def findCabalFile(): File =
    getProject.getProjectDir.listFiles().find(_.getName.endsWith(".cabal")) match {
      case Some(file) => file
      case None => throw new GradleException(s"Could not find any .cabal files in ${getProject.getRootDir.getAbsolutePath}")
    }
}

