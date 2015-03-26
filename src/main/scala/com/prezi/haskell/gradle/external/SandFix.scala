package com.prezi.haskell.gradle.external

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools.OptEnvConfigurer
import com.prezi.haskell.gradle.model.Sandbox
import com.prezi.haskell.gradle.ApiHelper._

class SandFix(sandFixPath: File, haskellTools: HaskellTools) {

  def run(envConfigurer: OptEnvConfigurer, sandbox: Sandbox, others: List[Sandbox]): Unit = {
    val dbArgs = others.map(child => child.asPackageDbArg)

    haskellTools.runHaskell(
      envConfigurer,
      sandFixPath </> "SandFix.hs",
      List( sandbox.root.getAbsolutePath,
        "packages",
        "--package-db=global")
        ::: dbArgs.toList : _*)

  }
}
