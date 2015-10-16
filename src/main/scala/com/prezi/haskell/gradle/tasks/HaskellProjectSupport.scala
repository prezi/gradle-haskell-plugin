package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.extension.HaskellExtension
import org.gradle.api.Task

/**
 * Mixin for tasks working on Haskell projects
 */
trait HaskellProjectSupport {
  this: Task =>

  protected def haskellExtension: HaskellExtension =
    getProject.getExtensions.getByType(classOf[HaskellExtension])
}
