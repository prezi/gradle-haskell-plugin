package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension._
import com.prezi.haskell.gradle.external.{Git, HaskellTools}
import com.prezi.haskell.gradle.model.ProjectSandboxStore
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator

trait HaskellProjectImpl {
  this: ProjectExtender =>

  protected def instantiator: Instantiator
  protected def fileResolver: FileResolver

  val sandFixPath = project.getBuildDir </> "sandfix"

  // Helpers
  protected def addFields(): Unit = {

    val tools = new HaskellTools(project.exec)
    addField("haskellTools", tools)
    addField("sandboxStore", new ProjectSandboxStore(project.getRootProject, Some(sandFixPath), getField[HaskellExtension]("haskell"), tools, haskellExtension.getUseStack))

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
    if (haskellExtension.getUseStack) {
      new StackSupport(project)
    }
  }
}
