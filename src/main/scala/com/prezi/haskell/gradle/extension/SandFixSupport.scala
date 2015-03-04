package com.prezi.haskell.gradle.extension

import java.io.File

import com.prezi.haskell.gradle.extension.impl.SandFixSupportImpl
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.tasks.{CopySandFix, FixDependentSandboxes}
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


