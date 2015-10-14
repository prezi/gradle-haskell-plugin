package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.impl.HaskellProjectImpl
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
  addSandboxTasks
  addCompilation
  addArtifacts
  addStackSupport // TODO: only in stack mode
}


