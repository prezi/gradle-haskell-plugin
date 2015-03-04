package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.impl.HaskellCompilationSupportImpl
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator

/**
 * Adds source sets and compile tasks to a project
 * @param project The project the plugin is applied to
 * @param instantiator Gradle object instantiator
 * @param fileResolver File resolver, needed for the source sets
 */
class HaskellCompilationSupport(
  protected val project: Project,
  protected val instantiator: Instantiator,
  protected val fileResolver: FileResolver)
  extends HaskellCompilationSupportImpl with ProjectExtender {

  registerExtension
  addSourceSets
  addCompileTasks
  addTestTasks
  extendCleanTask
}

