package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask

/**
 * Trait for tasks that executes stack
 */
trait StackExecTask
  extends DefaultTask
  with HaskellProjectSupport
  with HaskellDependencies
  with UsingHaskellTools
  with UsesSandbox {

  protected lazy val stackRoot: Option[String] = haskellExtension.getStackRoot

  protected val useProfiling: Boolean = haskellExtension.getProfiling
}
