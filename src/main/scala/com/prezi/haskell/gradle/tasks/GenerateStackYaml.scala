package com.prezi.haskell.gradle.tasks

import java.io.File
import scala.collection.JavaConverters._
import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.SnapshotVersions
import com.prezi.haskell.gradle.model.Sandbox
import org.apache.commons.io.FileUtils
import org.gradle.api.{GradleException, DefaultTask}
import org.gradle.api.tasks.TaskAction

import scala.util.{Failure, Success, Try}

class GenerateStackYaml
  extends DefaultTask
  with HaskellProjectSupport
  with HaskellDependencies
  with UsingHaskellTools
  with UsingGit
  with DependsOnStoreDependentSandboxes
  with TaskLogging {

  // TODO: depend on .cabal

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

    if (targetFile.isEmpty) {
      throw new IllegalStateException("targetFile is not specified")
    }

    debug(s"GenerateStackYaml dependentSandboxes: $dependentSandboxes")
    val yamlFile = generateContent(dependentSandboxes)
    FileUtils.writeStringToFile(targetFile.get, yamlFile)
  }

  private def generateContent(sandboxes: List[Sandbox]): String = {
    val content = new StringBuilder()
    val resolver = s"${haskellExtension.ghcVersion}"

    val pkgFlags = haskellExtension
      .getPackageFlags
      .asScala
      .filter(!_._2.isEmpty)

    if (pkgFlags.nonEmpty) {
      content.append("flags:\n")

      for ((pkgName, jflags) <- pkgFlags) {
        val flags = jflags.asScala

        content.append(s"  $pkgName:\n")
        for ((k, v) <- flags) {
          content.append(s"    $k: $v\n")
        }
      }
    }

    content.append(
      s"""packages:
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

    val isOffline = getProject.getGradle.getStartParameter.isOffline
    val snapshotVersions = new SnapshotVersions(isOffline, haskellExtension.getEnvConfigurer, getProject.exec, tools.get, git.get)
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

    val binPath = new File(new File(new File(getProject.getBuildDir, "sandbox"), "files"), "bin")
    binPath.mkdirs()
    content.append(s"local-bin-path: ${binPath.getAbsolutePath}")

    content.mkString
  }

  private def findCabalFile(): File =
    getProject.getProjectDir.listFiles().find(_.getName.endsWith(".cabal")) match {
      case Some(file) => file
      case None => throw new GradleException(s"Could not find any .cabal files in ${getProject.getRootDir.getAbsolutePath}")
    }
}

