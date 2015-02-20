package com.prezi.haskell.gradle

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks.{FixDependentSandboxes, CopySandFix}
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

/**
 * Adds SandFix support for the project
 *
 * @param project The project the plugin is applied to
 */
class SandFixSupport(protected val project: Project) extends SandFixSupportImpl with ProjectExtender {

  addCopySandFixTask
  addFixDependentSandboxesTask
}

trait SandFixSupportImpl {
  this : ProjectExtender =>

  protected val sandFixConfig = addConfiguration
  private val sandFixDir = new File(project.getBuildDir, "sandfix")

  private def addConfiguration(): Configuration = {
    val sandfixConfig = addConfiguration(Names.sandFixConfiguration)
    sandfixConfig.setVisible(false)

    sandfixConfig.getDependencies.add(
      new DefaultExternalModuleDependency("com.prezi.engine.dom", "pdom-sandfix", "1.+", "haskellSrc")
    )

    sandfixConfig
  }

  protected def addCopySandFixTask(): Unit = {
    val task = project.getTasks.create("copySandFix", classOf[CopySandFix])
    task.setUp(sandFixConfig, sandFixDir)
  }

  protected def addFixDependentSandboxesTask(): Unit = {
    val task = project.getTasks.create("fixDependentSandboxes", classOf[FixDependentSandboxes])
    task.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))
    task.tools = Some(getField[HaskellTools]("haskellTools"))
    task.sandFixPath = Some(new File(sandFixDir, "sandfix-1.1"))
  }
}
