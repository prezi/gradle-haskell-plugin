package com.prezi.haskell.gradle.tasks

import java.lang.Boolean

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.incubating.FunctionalSourceSet
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.{FileVisitDetails, FileVisitor}
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * Executes cabal install with the proper sandbox chaining
 */
class CompileTask extends CabalExecTask {
  val buildDir = getProject.getProjectDir </> "dist"

  dependsOn("sandbox")
  dependsOn("storeDependentSandboxes")

  getOutputs.upToDateWhen { _: AnyRef =>
    (for {
      storeDependentSandboxesTask <- Try { getProject.getTasksByName("storeDependentSandboxes", false).iterator().next() }
      isAnySandboxUpdated <- Try { storeDependentSandboxesTask.asInstanceOf[StoreDependentSandboxes].isAnySandboxUpdated }
    } yield isAnySandboxUpdated) match {
      case Success(bool) => new Boolean(!bool)
      case Failure(e) => throw new RuntimeException("Failed to get storeDependentSandboxes.anySandboxUpdated!", e)
    }
  }

  def attachToSourceSet(sourceSet: FunctionalSourceSet) = {

    for (lss <- sourceSet.asScala) {
      lss.getSource.visit(new FileVisitor {
        override def visitDir(fileVisitDetails: FileVisitDetails): Unit =
          getInputs.dir(fileVisitDetails.getFile)

        override def visitFile(fileVisitDetails: FileVisitDetails): Unit =
          getInputs.file(fileVisitDetails.getFile)
      })
    }

    dependsOn(sourceSet)
    getOutputs.dir(getProject.getExtensions.getByType(classOf[Sandbox]).root)
  }

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet
    needsToolsSet

    val ctx = cabalContext()
    tools.get.cabalInstall(ctx, depsOnly = true)
    tools.get.cabalConfigure(ctx)
    tools.get.cabalBuild(ctx)
    tools.get.cabalCopy(ctx)
    tools.get.cabalRegister(ctx)
  }
}
