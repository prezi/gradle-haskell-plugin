package com.prezi.haskell.gradle

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks._
import org.gradle.api.Project

/**
 * Adds sandbox support for a project
 *
 * @param project The project the plugin is applied to
 */
class SandboxSupport(protected val project: Project) extends SandboxSupportImpl with ProjectExtender {

  addSandboxInfoTask
  addSandboxDirectoriesTask
  addSandboxTask
  addSandboxPackagesTask
  addExtractDependentSandboxesTask
}

trait SandboxSupportImpl {
  this : ProjectExtender =>

  protected def addSandboxInfoTask(): Unit = {
    project.getTasks.create("sandboxInfo", classOf[SandboxInfo])
  }

  protected def addSandboxDirectoriesTask(): Unit = {
    project.getTasks.create("sandboxDirectories", classOf[SandboxDirectories])
  }

  protected def addSandboxTask(): Unit = {
    val task = project.getTasks.create("sandbox", classOf[SandboxTask])
    task.tools = Some(getField[HaskellTools]("haskellTools"))
  }

  protected def addSandboxPackagesTask(): Unit = {
    val task = project.getTasks.create("sandboxPackages", classOf[SandboxPackages])
    task.tools = Some(getField[HaskellTools]("haskellTools"))
  }

  protected def addExtractDependentSandboxesTask(): Unit = {
    val task = project.getTasks.create("extractDependentSandboxes", classOf[ExtractDependentSandboxes])
    task.configuration = Some(project.getConfigurations.getByName(Names.mainConfiguration))
  }
}