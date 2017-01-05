package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper
import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.incubating.FunctionalSourceSet
import com.prezi.haskell.gradle.model.StackOutputHash
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.{FileVisitDetails, FileVisitor}
import org.gradle.api.tasks.{Input, TaskAction}

import scala.collection.JavaConverters._

/**
 * Executes cabal install with the proper sandbox chaining
 */
class CompileTask
  extends CabalExecTask
  with DependsOnStoreDependentSandboxes
  with TaskLogging {

  val buildDir = getProject.getProjectDir </> "dist"
  var parallelThreadCount = 3

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

    if (haskellExtension.getUseStack) {
      getOutputs.upToDateWhen(ApiHelper.asClosureWithReturn { _: AnyRef =>

        val snapshotPath = getProject.getBuildDir </> "stack-output-snapshot"
        debug(s"$getName Custom up to date check based on ${sandbox.root}, with snapshot file $snapshotPath")

        val oldHashes = StackOutputHash.loadSnapshot(snapshotPath)

        oldHashes match {
          case Some(hashes) =>
            val newHashes = StackOutputHash.calculate(sandbox.root)
            newHashes.saveSnapshot(snapshotPath)

            val differs = hashes.differsFrom(newHashes)
            debug(s"$getName output snapshots differ: $differs")

            new java.lang.Boolean(!differs)
          case None =>
            debug(s"$getName is not up to date, there were no saved output snapshot")
            java.lang.Boolean.FALSE
        }
      })
    } else {
      debug(s"$getName output dir: $configTimeSandboxRoot")
      getOutputs.dir(configTimeSandboxRoot)
    }
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
      updateSnapshot()
    } else {
      runWithCabal()
    }
  }

  private def runWithStack(): Unit = {
    tools.get.stack(stackRoot, cabalContext().envConfigurer, Some(getProject.getProjectDir), "setup")

    val profilingArgs = if (cabalContext().profiling) {
      List("--executable-profiling", "--library-profiling")
    } else {
      List()
    }

    val ghcOptions = if (parallelThreadCount > 1) {
      List(s"""--ghc-options="-j${parallelThreadCount}"""")
    } else {
      List()
    }

    tools.get.stack(stackRoot, cabalContext().envConfigurer, Some(getProject.getProjectDir),
      "build" :: "--copy-bins" :: ghcOptions ::: profilingArgs : _*)
  }

  private def updateSnapshot(): Unit = {
    val snapshotPath = getProject.getBuildDir </> "stack-output-snapshot"
    val newHashes = StackOutputHash.calculate(sandbox.root)
    newHashes.saveSnapshot(snapshotPath)
  }

  private def runWithCabal(): Unit = {
    val ctx = cabalContext()
    tools.get.cabalInstall(ctx, depsOnly = true)
    tools.get.cabalConfigure(ctx)
    tools.get.cabalBuild(ctx)
    tools.get.cabalCopy(ctx)
    tools.get.cabalRegister(ctx)
  }

  @Input
  def getParallelThreadCount: Integer = parallelThreadCount
  def setParallelThreadCount(threadCount: Integer): Unit = {
    parallelThreadCount = threadCount
  }
  def parallelThreadCount(threadCount: Integer): Unit = setParallelThreadCount(threadCount)
}
