package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.Task

/**
 * Mixin for tasks working on Haskell projects
 */
trait HaskellProjectSupport {
  this: Task =>

  protected def sandbox: Sandbox =
    getProject.getExtensions.getByType(classOf[Sandbox])
}
