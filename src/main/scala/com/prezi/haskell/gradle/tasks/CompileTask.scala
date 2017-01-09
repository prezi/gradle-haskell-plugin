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
  extends StackExecTask
    with DependsOnStoreDependentSandboxes
    with TaskLogging {

  private var parallelThreadCount = 3

  dependsOn("generateStackYaml")

  def attachToSourceSet(sourceSet: FunctionalSourceSet): Unit = {

    for (lss <- sourceSet.asScala) {
      lss.getSource.visit(new FileVisitor {
        override def visitDir(fileVisitDetails: FileVisitDetails): Unit = {
          debug(s"$getName input dir: ${fileVisitDetails.getFile.getAbsolutePath}")
          getInputs.dir(fileVisitDetails.getFile)
        }

        override def visitFile(fileVisitDetails: FileVisitDetails): Unit = {
          debug(s"$getName input file: ${fileVisitDetails.getFile.getAbsolutePath}")
          getInputs.file(fileVisitDetails.getFile)
        }
      })
    }

    dependsOn(sourceSet)

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
  }

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    runWithStack()
    updateSnapshot()
  }

  private def runWithStack(): Unit = {
    val profilingArgs = if (useProfiling) {
      List("--executable-profiling", "--library-profiling")
    } else {
      List()
    }

    val ghcOptions = if (parallelThreadCount > 1) {
      List(s"""--ghc-options="-j$parallelThreadCount"""")
    } else {
      List()
    }

    tools.get.stack(stackRoot, Some(getProject.getProjectDir),
      "build" :: "--copy-bins" :: ghcOptions ::: profilingArgs: _*)
  }

  private def updateSnapshot(): Unit = {
    val snapshotPath = getProject.getBuildDir </> "stack-output-snapshot"
    val newHashes = StackOutputHash.calculate(sandbox.root)
    newHashes.saveSnapshot(snapshotPath)
  }

  @Input
  def getParallelThreadCount: Integer = parallelThreadCount

  def setParallelThreadCount(threadCount: Integer): Unit = {
    parallelThreadCount = threadCount
  }

  def parallelThreadCount(threadCount: Integer): Unit = setParallelThreadCount(threadCount)
}
