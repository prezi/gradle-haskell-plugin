package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.incubating.FunctionalSourceSet
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.{FileVisitDetails, FileVisitor}
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

/**
 * Executes cabal install with the proper sandbox chaining
 */
class CompileTask
  extends CabalExecTask
  with DependsOnStoreDependentSandboxes
  with TaskLogging {

  val buildDir = getProject.getProjectDir </> "dist"

  if (haskellExtension.getUseStack) {
    dependsOn("generateStackYaml")
  } else {
    dependsOn("sandbox")
  }

  def attachToSourceSet(sourceSet: FunctionalSourceSet) = {

    for (lss <- sourceSet.asScala) {
      lss.getSource.visit(new FileVisitor {
        override def visitDir(fileVisitDetails: FileVisitDetails): Unit = {
          debug(s"${getName} input dir: ${fileVisitDetails.getFile.getAbsolutePath}")
          getInputs.dir(fileVisitDetails.getFile)
        }

        override def visitFile(fileVisitDetails: FileVisitDetails): Unit = {
          debug(s"${getName} input file: ${fileVisitDetails.getFile.getAbsolutePath}")
          getInputs.file(fileVisitDetails.getFile)
        }
      })
    }

    dependsOn(sourceSet)

    debug(s"${getName} output dir: $configTimeSandboxRoot")
    getOutputs.dir(configTimeSandboxRoot)
  }

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    if (haskellExtension.getUseStack) {
      runWithStack()
    } else {
      runWithCabal()
    }
  }

  def runWithStack(): Unit = {
    tools.get.stack(cabalContext().envConfigurer, getProject.getProjectDir, "--no-system-ghc", "setup")

    val profilingArgs = if (cabalContext().profiling) {
      List("--executable-profiling", "--library-profiling")
    } else {
      List()
    }
    tools.get.stack(cabalContext().envConfigurer, getProject.getProjectDir, "--no-system-ghc" :: "build" :: "--copy-bins" :: profilingArgs : _*)
  }

  def runWithCabal(): Unit = {
    val ctx = cabalContext()
    tools.get.cabalInstall(ctx, depsOnly = true)
    tools.get.cabalConfigure(ctx)
    tools.get.cabalBuild(ctx)
    tools.get.cabalCopy(ctx)
    tools.get.cabalRegister(ctx)
  }
}
