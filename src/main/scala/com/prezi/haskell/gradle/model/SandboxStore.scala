package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.extension.HaskellExtension
import com.prezi.haskell.gradle.external.{HaskellTools, SandFix}
import org.gradle.api.{GradleException, Project}
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.TaskExecutionException

trait SandboxStore {

  def root: File
  def store(depSandbox: SandboxArtifact, dependencies: Set[SandboxArtifact])
  def get(sandbox: SandboxArtifact): Sandbox
}

class ProjectSandboxStore(project: Project, sandFixPath: Option[File], exts: => HaskellExtension, tools: => HaskellTools) extends SandboxStore {

  val root = project.getBuildDir

  private def finalSandFixPath = sandFixPath.getOrElse(project.getBuildDir </> "sandfix")

  private def find(depSandbox: SandboxArtifact) =
    depSandbox.toSandbox(root)

  override def get(depSandbox: SandboxArtifact): Sandbox = {
    val fixedSandbox = find(depSandbox)

    if (!fixedSandbox.root.exists())
      throw new GradleException(s"Required sandbox ${depSandbox.name} was not found at ${fixedSandbox.root.getAbsolutePath}")

    fixedSandbox
  }

  override def store(depSandbox: SandboxArtifact, dependencies: Set[SandboxArtifact]) = {
    val sandbox = find(depSandbox)
    if (!sandbox.root.exists()) {
      sandbox.root.mkdirs()
      
      project.getLogger.debug("Locking sandbox {}", sandbox.root.getAbsolutePath)
      if (sandbox.lock.createNewFile()) {
        try {
          project.getLogger.info("Extracting dependent sandbox {} to {}", depSandbox.name, sandbox.extractionRoot.getAbsolutePath)

          project.copy(asClosure { spec: CopySpec =>
            spec.from(project.zipTree(depSandbox.artifact))
            spec.into(sandbox.extractionRoot)
          })

          project.getLogger.info("Fixing dependent sandbox {}", depSandbox.name)

          val envConfigurer = exts.getEnvConfigurer
          val sandFix = new SandFix(project.exec, finalSandFixPath </> "SandFix.hs", tools)
          sandFix.run(envConfigurer, sandbox, dependencies.map(_.toSandbox(root)).toList)
          tools.ghcPkgRecache(envConfigurer, sandbox)
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
    }
  }
}
