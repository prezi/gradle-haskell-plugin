package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.impl.GhcModSupportImpl
import org.gradle.api.Project

/**
 * Adds support for ghc-mod
 *
 * This means adding a new task to the compilation phase that generates a
 * custom ghc-mod cradle file containing all the package databases required
 * to compile the project.
 *
 * @param project The project to give ghc-mod support to
 */
class GhcModSupport(protected val project: Project) extends GhcModSupportImpl with ProjectExtender {

  addTasks
}

