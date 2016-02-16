package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.external.HaskellTools.CabalContext
import org.gradle.api.DefaultTask

/**
 * Trait for tasks that executes cabal
 */
trait CabalExecTask
  extends DefaultTask
  with HaskellProjectSupport
  with HaskellDependencies
  with UsingHaskellTools
  with UsesSandbox {

  protected lazy val stackRoot: Option[String] = haskellExtension.getStackRoot

  def cabalContext(): CabalContext = new CabalContext(
    haskellExtension.cabalType,
    getProject.getProjectDir,
    sandbox,
    dependentSandboxes,
    haskellExtension.getProfiling,
    haskellExtension.getCabalConfigFile,
    haskellExtension.getEnvConfigurer
  )
}
