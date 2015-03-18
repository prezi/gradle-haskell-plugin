package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.impl.SandboxSupportImpl
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
  addCabalFreezeTask
}

