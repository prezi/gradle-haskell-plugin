package com.prezi.haskell.gradle.extension

import java.io.File

import com.prezi.haskell.gradle.extension.impl.SandboxSupportImpl
import org.gradle.api.Project

/**
 * Adds sandbox support for a project
 *
 * @param project The project the plugin is applied to
 */
class SandboxSupport(protected val project: Project, protected val sandFixDir: File) extends SandboxSupportImpl with ProjectExtender {

  addSandboxInfoTask
  addSandboxDirectoriesTask
  addSandboxTask
  addSandboxPackagesTask
  addStoreDependentSandboxesTask
  addCabalFreezeTask
  addCopySandFixTask
}

