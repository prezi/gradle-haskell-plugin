package com.prezi.haskell.gradle.extension.impl

import java.io.File

import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension._
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.{ProjectSandboxStore, Sandbox}
import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator

trait HaskellProjectImpl {
  this: ProjectExtender =>

  protected def instantiator: Instantiator

  protected def fileResolver: FileResolver

  val sandbox = new Sandbox(new File(project.getBuildDir, "sandbox"))
  val sandFixPath = project.getBuildDir </> "sandfix"

  // Helpers
  protected def addFields(): Unit = {

    addField("ghcSandbox", sandbox)
    addField("ghcSandboxRoot", sandbox.root)
    addField("ghcPackageDb", sandbox.packageDb)
    addField("ghcPrefix", sandbox.installPrefix)

    val tools = new HaskellTools(project.exec)
    addField("haskellTools", tools)
    addField("sandboxStore", new ProjectSandboxStore(project.getRootProject, Some(sandFixPath), getField[HaskellExtension]("haskell"), tools))
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
}
