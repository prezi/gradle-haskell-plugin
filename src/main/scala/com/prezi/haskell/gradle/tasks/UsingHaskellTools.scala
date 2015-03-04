package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.external.HaskellTools
import org.gradle.api.Task

/**
 * Mixin for tasks requiring a @HaskellTools instance to work with
 */
trait UsingHaskellTools {
  this: Task =>

  var tools: Option[HaskellTools] = None

  protected def needsToolsSet: Unit = {
    if (!tools.isDefined) {
      throw new IllegalStateException("tools is not specified")
    }
  }
}
