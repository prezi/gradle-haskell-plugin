package com.prezi.haskell.gradle.external

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.HaskellTools.CabalContext
import com.prezi.haskell.gradle.model.Sandbox
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Executes external tools with the Gradle project's execution command
 * @param executor The `project.exec` function
 */
class HaskellTools(executor : Action[ExecSpec] => ExecResult) {

  def cabalInstall(ctx: CabalContext): Unit = {
    exec(Some(ctx.root),
      None,
      "cabal",
      configFileArgs(ctx.configFile)
      ::: "install"
      :: "-j"
      :: "--package-db=clear"
      :: "--package-db=global"
      :: ctx.dependencies.map(_.asPackageDbArg)
      ::: List(ctx.targetSandbox.asPackageDbArg,
               ctx.targetSandbox.asPrefixArg)
      ::: profilingArgs(ctx.profiling)
      : _*)
  }

  def cabalTest(ctx: CabalContext): Unit = {
    exec(Some(ctx.root),
      None,
      "cabal",
      configFileArgs(ctx.configFile)
      ::: "configure"
      :: "--enable-tests"
      :: "--package-db=clear"
      :: "--package-db=global"
      :: ctx.dependencies.map(_.asPackageDbArg)
      ::: List(ctx.targetSandbox.asPackageDbArg,
               ctx.targetSandbox.asPrefixArg)
      ::: profilingArgs(ctx.profiling)
      : _*)
    exec(Some(ctx.root),None,  "cabal", "test")
  }

  def runHaskell(source: File, args: String*): Unit =
    exec(None, None, "runhaskell", source.getAbsolutePath +: args : _*)

  def ghcPkgRecache(sandbox: Sandbox): Unit =
    exec(None, None, "ghc-pkg", "-f", sandbox.packageDb.getAbsolutePath, "recache")

  def ghcPkgList(sandboxes: List[Sandbox]): Unit =
    exec(None, None, "ghc-pkg", "list" :: sandboxes.map(_.asPackageDbArg) : _*)

  private def exec(workDir: Option[File], envConfigurer: Option[Closure[Map[String, Object]]], program: String, args: String*): Unit = {
    executor(asAction({ spec: ExecSpec =>
      spec.commandLine(program +: args.toSeq : _*)

      envConfigurer map { _.call(spec.getEnvironment()) }
      workDir map { spec.workingDir(_) }
    }))
  }

  private def profilingArgs(profiling: Boolean): List[String] = {
    if (profiling) {
      List("--enable-profiling", "--enable-library-profiling")
    } else {
      List()
    }
  }

  private def configFileArgs(configFile: Option[String]): List[String] =
    configFile match {
      case Some(cf) => List("--config-file", cf)
      case None => Nil
    }
}

object HaskellTools {
  case class CabalContext(
    root: File,
    targetSandbox: Sandbox,
    dependencies: List[Sandbox],
    profiling: Boolean,
    configFile: Option[String]
  )
}