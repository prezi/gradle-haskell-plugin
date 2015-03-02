package com.prezi.haskell.gradle

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator

/**
 * Main entry point of the haskell plugin,
 *
 * Extends a gradle project with fields, configurations and tasks
 * @param project The project the plugin is applied on
 */
class HaskellProject(
    protected val project: Project,
    protected val instantiator: Instantiator,
    protected val fileResolver: FileResolver)
  extends HaskellProjectImpl with ProjectExtender {
  // Integrating haskell support to project
  addFields
  addConfigurations
  addSandFix
  addSandboxTasks
  addSourceSets
}

trait HaskellProjectImpl {
  this : ProjectExtender =>

  protected def instantiator: Instantiator
  protected def fileResolver: FileResolver

  val sandbox = new Sandbox(new File(project.getBuildDir, "sandbox"))

  // Helpers
  protected def addFields(): Unit = {

    addField("ghcSandbox", sandbox)
    addField("ghcSandboxRoot", sandbox.root)
    addField("ghcPackageDb", sandbox.packageDb)
    addField("ghcPrefix", sandbox.installPrefix)
    addField("haskellTools", new HaskellTools(project.exec))
  }

  protected def addConfigurations(): Unit = {
    val mainConfig = addConfiguration(Names.mainConfiguration)
    val testConfig = addConfiguration(Names.testConfiguration)

    testConfig.extendsFrom(mainConfig)
  }

  protected def addSandFix(): Unit = {
    new SandFixSupport(project)
  }

  protected def addSandboxTasks(): Unit = {
    new SandboxSupport(project)
  }

  protected def addSourceSets(): Unit = {
    new HaskellCompilationSupport(project, instantiator, fileResolver)
  }
}
