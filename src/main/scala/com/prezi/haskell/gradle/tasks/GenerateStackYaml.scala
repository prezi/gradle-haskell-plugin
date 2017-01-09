package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.SnapshotVersions
import com.prezi.haskell.gradle.model.{GHC801WithSierraFix, Sandbox, StackYamlWriter}
import com.prezi.haskell.gradle.util.FileLock
import org.gradle.api.tasks.TaskAction
import org.gradle.api.{DefaultTask, GradleException}

import scala.collection.JavaConverters._
import resource._

class GenerateStackYaml
  extends DefaultTask
  with HaskellProjectSupport
  with HaskellDependencies
  with UsingHaskellTools
  with UsingGit
  with DependsOnStoreDependentSandboxes
  with TaskLogging {

  private var targetFile_ : Option[File] = None

  findCabalFile() match {
    case Some(cabalFile) => getInputs.file(cabalFile)
    case None =>
  }

  def targetFile = targetFile_
  def targetFile_=(value: Option[File]): Unit = {
    targetFile_ = value

    if (value.isDefined) {
      getOutputs.file(value.get)
    }
  }

  @TaskAction
  def run(): Unit = {
    val fileLock = new FileLock(new File(getProject.getRootProject.getBuildDir, "generate-yaml.lock"))
    fileLock.lock()
    try {
      needsConfigurationSet
      needsGitSet
      needsToolsSet

      if (targetFile.isEmpty) {
        throw new IllegalStateException("targetFile is not specified")
      }

      debug(s"GenerateStackYaml dependentSandboxes: $dependentSandboxes")
      generateContent(targetFile.get, dependentSandboxes)
    } finally {
      fileLock.release()
    }
  }

  private def generateContent(target: File, sandboxes: List[Sandbox]): Unit = {
    for (builder <- managed(new StackYamlWriter(target))) {
      val resolver = s"${haskellExtension.ghcVersion}"

      val pkgFlags = haskellExtension
        .getPackageFlags
        .asScala
        .toMap
        .filter { case (_, value) => !value.isEmpty }
        .map { case (key, value) => (key, value.asScala.toMap) }

      if (pkgFlags.nonEmpty) {
        builder.flags(pkgFlags)
      }

      builder.packages(List("."))
      builder.resolver(resolver)
      builder.extraPackageDbs(sandboxes.map(_.packageDb.getAbsolutePath))


      val isOffline = getProject.getGradle.getStartParameter.isOffline
      val snapshotVersions = new SnapshotVersions(isOffline, haskellExtension.getOverriddenSnapshotVersionsCacheDir.map(path => new File(path)), haskellExtension.getStackRoot, getProject.exec, tools.get, git.get)
      val deps = snapshotVersions.run(haskellExtension.snapshotId, getCabalFile())

      builder.extraDeps(deps)

      val binPath = getProject.getBuildDir </> "sandbox" </> "files" </> "bin"
      binPath.mkdirs()
      builder.localBinPath(binPath.getAbsolutePath)

      // TODO: use configured GHC version
      builder.ghcVersion(GHC801WithSierraFix)
    }
  }

  private def findCabalFile(): Option[File] =
    getProject.getProjectDir.listFiles().find(_.getName.endsWith(".cabal"))

  private def getCabalFile(): File =
    findCabalFile() match {
      case Some(file) => file
      case None => throw new GradleException(s"Could not find any .cabal files in ${getProject.getRootDir.getAbsolutePath}")
    }
}

