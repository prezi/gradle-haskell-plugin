package com.prezi.haskell.gradle.external

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Executes external tools with the Gradle project's execution command
 * @param executor The `project.exec` function
 */
class HaskellTools(executor : Action[ExecSpec] => ExecResult) {
  def cabalInstall(root: File, buildDir: File, targetSandbox: Sandbox, dependencies: List[Sandbox]): Unit = {
    exec(Some(root),
      "cabal", "install"
      :: "--package-db=clear"
      :: "--package-db=global"
      :: dependencies.map(_.asPackageDbArg)
      ::: List(targetSandbox.asPackageDbArg,
               targetSandbox.asPrefixArg,
               s"--builddir=${buildDir.getAbsolutePath}")
      : _*)
  }

  def runHaskell(source: File, args: String*): Unit =
    exec(None, "runhaskell", source.getAbsolutePath +: args : _*)

  def ghcPkgRecache(sandbox: Sandbox): Unit =
    exec(None, "ghc-pkg", "-f", sandbox.packageDb.getAbsolutePath, "recache")

  def ghcPkgList(sandboxes: List[Sandbox]): Unit =
    exec(None, "ghc-pkg", "list" :: sandboxes.map(_.asPackageDbArg) : _*)

  private def exec(workDir: Option[File], program: String, args: String*): Unit = {
    executor(asAction({ spec: ExecSpec =>
      spec.commandLine(program +: args.toSeq : _*)

      if (workDir.isDefined) {
        spec.workingDir(workDir.get)
      }
    }))
  }
}
