package com.prezi.haskell.gradle.external

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.HaskellTools._
import com.prezi.haskell.gradle.model.Sandbox
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Executes external tools with the Gradle project's execution command
 * @param executor The `project.exec` function
 */
class HaskellTools(executor : Action[ExecSpec] => ExecResult) {

  def cabalConfigure(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: "configure"
        :: "--enable-tests"
        :: "--disable-executable-stripping"
        :: "--disable-library-stripping"
        :: "--package-db=clear"
        :: "--package-db=global"
        :: ctx.dependencies.map(_.asPackageDbArg)
        ::: List(ctx.targetSandbox.asPackageDbArg,
        ctx.targetSandbox.asPrefixArg)
        ::: profilingArgs(ctx.profiling, ctx.version)
        : _*)
  }

  def cabalInstall(ctx: CabalContext, depsOnly: Boolean): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
      ::: "install"
      :: "-j"
      :: "--enable-tests"
      :: "--package-db=clear"
      :: "--package-db=global"
      :: ctx.dependencies.map(_.asPackageDbArg)
      ::: List(ctx.targetSandbox.asPackageDbArg,
               ctx.targetSandbox.asPrefixArg)
      ::: profilingArgs(ctx.profiling, ctx.version)
      ::: onlyDepsArgs(depsOnly)
      : _*)
  }

  def cabalBuild(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: List("build", "-j")
        : _*)
  }

  def cabalCopy(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
      ::: List("copy")
      : _*)
  }

  def cabalRegister(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: List("register")
        : _*)
  }

  def cabalTest(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
      ::: List("test", "--show-details=streaming")
      : _*)
  }

  def cabalFreeze(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: List("freeze")
        : _*)
  }

  def cabalREPL(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: List("repl")
        : _*)
  }

  def runHaskell(envConfigurer: OptEnvConfigurer, source: File, args: String*): Unit =
    exec(
      None,
      envConfigurer,
      "runhaskell",
      source.getAbsolutePath +: args : _*)

  def ghc(envConfigurer: OptEnvConfigurer, args: String*): Unit =
    exec(
      None,
      envConfigurer,
      "ghc",
      args : _*)

  def ghcPkgRecache(envConfigurer: OptEnvConfigurer, sandbox: Sandbox): Unit =
    exec(
      None,
      envConfigurer,
      "ghc-pkg",
      "-f",
      sandbox.packageDb.getAbsolutePath,
      "recache")

  def ghcPkgList(envConfigurer: OptEnvConfigurer, sandboxes: List[Sandbox]): Unit =
    exec(
      None,
      envConfigurer,
      "ghc-pkg",
      "list" :: sandboxes.map(_.asPackageDbArg) : _*)

  private def exec(workDir: Option[File], envConfigurer: OptEnvConfigurer, program: String, args: String*): Unit = {
    executor(asAction({ spec: ExecSpec =>
      // TODO: check for os?
      // Wrapping commands in "sh" if needed so they can use the
      // new PATH created by the envConfigurer
      val cmdLine: Seq[String] = envConfigurer match {
        case Some(_) => Seq("sh", "-c",  (program :: args.toList).mkString(" "))
        case None => program +: args.toSeq
      }

      spec.commandLine(cmdLine : _*)

      envConfigurer map { _.call(spec.getEnvironment()) }
      workDir map { spec.workingDir(_) }
    }))
  }

  private def profilingArgs(profiling: Boolean, cabalVersion: CabalVersion): List[String] = {
    if (profiling) {
      List("--enable-library-profiling")
    } else {
      List()
    }
  }

  private def onlyDepsArgs(onlyDeps: Boolean): List[String] = {
    if (onlyDeps) {
      List("--only-dependencies")
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
  type EnvConfigurer = Closure[AnyRef]
  type OptEnvConfigurer = Option[Closure[AnyRef]]

  abstract class CabalVersion
  object CabalVersion {
    def parse(version: String): CabalVersion = {
      version match {
        case "1.20" => Cabal120
        case _ => Cabal122
      }
    }
  }

  case object Cabal120 extends CabalVersion
  case object Cabal122 extends CabalVersion

  case class CabalContext(
    version: CabalVersion,
    root: File,
    targetSandbox: Sandbox,
    dependencies: List[Sandbox],
    profiling: Boolean,
    configFile: Option[String],
    envConfigurer: OptEnvConfigurer)
}