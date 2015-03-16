package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import com.prezi.haskell.gradle.external.HaskellTools.{Cabal122, CabalContext}

/**
 * Trait for tasks that executes cabal
 */
trait CabalExecTask extends DefaultTask with HaskellProjectSupport with HaskellDependencies with UsingHaskellTools {
  def cabalContext(): CabalContext = CabalContext(
    Cabal122,
    getProject.getProjectDir,
    sandbox,
    dependentSandboxes,
    haskellExtension.getProfiling,
    haskellExtension.getCabalConfigFile,
    haskellExtension.getEnvConfigurer
  )
}
