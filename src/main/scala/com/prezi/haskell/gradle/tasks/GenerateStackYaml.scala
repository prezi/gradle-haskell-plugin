package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.SnapshotVersions
import com.prezi.haskell.gradle.model.Sandbox
import com.prezi.haskell.gradle.util.FileLock
import org.apache.commons.io.FileUtils
import org.gradle.api.tasks.TaskAction
import org.gradle.api.{DefaultTask, GradleException}

import scala.collection.JavaConverters._

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
      val yamlFile = generateContent(dependentSandboxes)
      FileUtils.writeStringToFile(targetFile.get, yamlFile)
    } finally {
      fileLock.release()
    }
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
    val snapshotVersions = new SnapshotVersions(isOffline, haskellExtension.getOverriddenSnapshotVersionsCacheDir.map(path => new File(path)), haskellExtension.getStackRoot, haskellExtension.getEnvConfigurer, getProject.exec, tools.get, git.get)
    val deps = snapshotVersions.run(haskellExtension.snapshotId, getCabalFile())

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

    val binPath = getProject.getBuildDir </> "sandbox" </> "files" </> "bin"
    binPath.mkdirs()
    content.append(s"local-bin-path: ${binPath.getAbsolutePath}")

    temporarySierraFix(content)

    content.mkString
  }

  private def findCabalFile(): Option[File] =
    getProject.getProjectDir.listFiles().find(_.getName.endsWith(".cabal"))

  private def getCabalFile(): File =
    findCabalFile() match {
      case Some(file) => file
      case None => throw new GradleException(s"Could not find any .cabal files in ${getProject.getRootDir.getAbsolutePath}")
    }

  private def temporarySierraFix(content: StringBuilder): Unit = {
    // Temporary fix for https://github.com/commercialhaskell/stack/issues/2577

    content.append(
      """
        |compiler-check: match-exact
        |compiler: ghc-8.0.1.20161117
        |setup-info:
        |  ghc:
        |    linux64:
        |      8.0.1.20161117:
        |        url: http://downloads.haskell.org/~ghc/8.0.2-rc1/ghc-8.0.1.20161117-x86_64-deb8-linux.tar.xz
        |        content-length: 112047972
        |        sha1: 6a6e4c9c53c71cc84b6966a9f61948542fd2f15a
        |    macosx:
        |      8.0.1.20161117:
        |        url: https://downloads.haskell.org/~ghc/8.0.2-rc1/ghc-8.0.1.20161117-x86_64-apple-darwin.tar.xz
        |        content-length: 113379688
        |        sha1: 53ed03d986a49ea680c291540ce44ce469514d7c
        |    windows64:
        |      8.0.1.20161117:
        |        url: https://downloads.haskell.org/~ghc/8.0.2-rc1/ghc-8.0.1.20161117-x86_64-unknown-mingw32.tar.xz
        |        content-length: 155652048
        |        sha1: 74118dd8fd8b5e4c69b25df1644273fbe13177c7
      """.stripMargin)
  }
}

