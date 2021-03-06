package com.prezi.haskell.gradle.model.sandboxstore

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Profiling.measureTime
import com.prezi.haskell.gradle.extension.HaskellExtension
import com.prezi.haskell.gradle.external.{HaskellTools, SandFix}
import com.prezi.haskell.gradle.io.packers.Unpacker
import com.prezi.haskell.gradle.model.{Sandbox, SandboxArtifact}
import org.gradle.api.{GradleException, Project}

/**
  * Sandbox store implementation with the following properties
  *
  * - It stores the sandboxes in a subdirectory of a Gradle project's build directory
  * - Runs the SandFix tool on newly extracted sandboxes
  *
  * @param project     The root project where the sandboxes will be stored
  * @param sandFixPath Path to the SandFix tool
  * @param unpacker    Zip unpacker to be used to extract sandboxes
  * @param exts
  * @param tools
  */
class ProjectSandboxStore(project: Project, sandFixPath: Option[File], unpacker: Unpacker, exts: => HaskellExtension, tools: => HaskellTools) extends SandboxStore {

  private val root = project.getBuildDir
  private lazy val finalSandFixPath = sandFixPath.getOrElse(project.getBuildDir </> "sandfix")
  private lazy val sandFix = new SandFix(finalSandFixPath </> "SandFix.hs", tools)

  override def find(depSandbox: SandboxArtifact): Sandbox =
    depSandbox.toStackSandbox(root)

  override def get(depSandbox: SandboxArtifact): Sandbox = {
    val fixedSandbox = find(depSandbox)

    if (!fixedSandbox.root.exists())
      throw new GradleException(s"Required sandbox ${depSandbox.name} was not found at ${fixedSandbox.root.getAbsolutePath}")

    fixedSandbox
  }

  override def store(stackRoot: Option[String], depSandbox: SandboxArtifact, dependencies: Set[SandboxArtifact]): SandBoxStoreResult = {
    val sandbox = find(depSandbox)
    if (!sandbox.root.exists()) {
      sandbox.root.mkdirs()

      project.getLogger.debug("Locking sandbox {}", sandbox.root.getAbsolutePath)
      if (sandbox.lock.createNewFile()) {
        try {
          extractSandbox(depSandbox, sandbox)
          fixSandbox(stackRoot, depSandbox, dependencies, sandbox)
          SandBoxStoreResult.Created
        }
        finally {
          project.getLogger.debug("Unlocking sandbox {}", sandbox.root.getAbsolutePath)
          sandbox.lock.delete()
        }
      }
      else {
        throw new GradleException(s"Could not store sandbox, store is locked by ${sandbox.lock.getAbsolutePath}")
      }
    } else {
      project.getLogger.info("Sandbox already exists at {}", sandbox.root.getAbsolutePath)
      SandBoxStoreResult.AlreadyExists
    }
  }

  def fixSandbox(stackRoot: Option[String], depSandbox: SandboxArtifact, dependencies: Set[SandboxArtifact], sandbox: Sandbox): Any = {
    // For executable artifacts we don't have a package db:
    if (sandbox.packageDb.exists()) {
      project.getLogger.info("Fixing dependent sandbox {}", depSandbox.name)

      val (_, elapsed) = measureTime {
        sandFix.run(sandbox, dependencies.map(get).toList, stackRoot)
      }
      project.getLogger.info("Fixed dependent sandbox {} in {} s", depSandbox.name, elapsed)

      val (_, elapsedRecache) = measureTime {
        tools.ghcPkgRecache(stackRoot, sandbox)
      }
      project.getLogger.info("ghc-pkg recache of sandbox {} in {} s", depSandbox.name, elapsedRecache)
    }
  }

  def extractSandbox(depSandbox: SandboxArtifact, sandbox: Sandbox): Unit = {
    project.getLogger.info("Extracting dependent sandbox {} to {}", depSandbox.name, sandbox.extractionRoot.getAbsolutePath)

    val (_, elapsed) = measureTime {
      unpacker.unpack(depSandbox.artifact, sandbox.extractionRoot)
    }

    project.getLogger.info(s"Extracted dependent sandbox ${depSandbox.name} to ${sandbox.extractionRoot.getAbsolutePath} in $elapsed s", List(): _*)
  }
}
