package com.prezi.haskell.gradle.extension.impl

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension._
import com.prezi.haskell.gradle.external.{Git, HaskellTools}
import com.prezi.haskell.gradle.io.packers.GradleZipPacker
import com.prezi.haskell.gradle.model.{GHC801WithSierraFix, StackYamlWriter}
import com.prezi.haskell.gradle.model.sandboxstore.ProjectSandboxStore
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import resource._

trait HaskellProjectImpl {
  this: ProjectExtender =>

  protected def instantiator: Instantiator

  protected def fileResolver: FileResolver

  val sandFixPath: File = project.getBuildDir </> "sandfix"

  val stackToolPath: File = project.getBuildDir </> "stack-tooling"

  // Helpers
  protected def addFields(): Unit = {

    val tools = new HaskellTools(project.exec, getStackToolPath())
    val unpacker = new GradleZipPacker(project)
    val sandboxStore = new ProjectSandboxStore(project.getRootProject, Some(sandFixPath), unpacker, getField[HaskellExtension]("haskell"), tools)
    addField("haskellTools", tools)
    addField("sandboxStore", sandboxStore)

    val git = new Git(project.exec)
    addField("git", git)
  }

  protected def addConfigurations(): Unit = {
    val mainConfig = addConfiguration(Names.mainConfiguration)
    val testConfig = addConfiguration(Names.testConfiguration)

    testConfig.extendsFrom(mainConfig)
  }

  protected def addSandboxTasks(): Unit = {
    new SandboxSupport(project, sandFixPath)
  }

  protected def addCompilation(): Unit = {
    new HaskellCompilationSupport(project, instantiator, fileResolver)
  }

  protected def addArtifacts(): Unit = {
    new ZippedSandboxArtifactSupport(project)
  }

  protected def addStackSupport(): Unit = {
    new StackSupport(project)
  }

  protected def registerExtension(): Unit = {
    createField[HaskellExtension]("haskell", instantiator, project)
  }

  private def getStackToolPath(): File = {
    if (stackToolPath.exists() && (stackToolPath </> "stack.yaml").exists()) {
      stackToolPath
    } else {
      stackToolPath.mkdirs()
      for (yaml <- managed(new StackYamlWriter(stackToolPath </> "stack.yaml"))) {
        // TODO: use configured GHC version
        yaml.resolver(haskellExtension.getGhcVersion)
        yaml.ghcVersion(GHC801WithSierraFix)
      }
      stackToolPath
    }
  }
}
