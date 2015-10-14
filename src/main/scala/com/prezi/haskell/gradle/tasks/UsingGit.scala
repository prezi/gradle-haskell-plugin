package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.external.Git
import org.gradle.api.Task

/**
 * Mixin for tasks requiring a @Git instance to work with
 */
trait UsingGit{
  this: Task =>

  var git: Option[Git] = None

  protected def needsGitSet: Unit = {
    if (!git.isDefined) {
      throw new IllegalStateException("git is not specified")
    }
  }
}
