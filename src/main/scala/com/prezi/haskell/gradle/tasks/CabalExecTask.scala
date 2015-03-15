package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import com.prezi.haskell.gradle.external.HaskellTools.CabalContext

/**
 * Trait for tasks that executes cabal
 */
trait CabalExecTask extends DefaultTask with HaskellProjectSupport with HaskellDependencies with UsingHaskellTools {
  def cabalContext(): CabalContext = CabalContext(
    getProject.getProjectDir,
    sandbox,
    dependentSandboxes,
    haskellExtension.getProfiling,
    haskellExtension.getCabalConfigFile,
    haskellExtension.getEnvConfigurer
  )
}
