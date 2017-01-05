package com.prezi.haskell.gradle.external

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools._
import com.prezi.haskell.gradle.external.ToolsBase.OptEnvConfigurer
import com.prezi.haskell.gradle.model.Sandbox
import com.prezi.haskell.gradle.ApiHelper.asClosure
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Executes external tools with the Gradle project's execution command
  *
  * @param executor The `project.exec` function
 */
class HaskellTools(executor : Action[ExecSpec] => ExecResult)
  extends ToolsBase(executor) {

  def cabalConfigure(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: "configure"
        :: "--enable-tests"
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

  def cabalUpdate(ctx: CabalContext): Unit = {
    exec(
      Some(ctx.root),
      ctx.envConfigurer,
      "cabal",
      configFileArgs(ctx.configFile)
        ::: List("update")
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
      "stack runhaskell --",
      source.getAbsolutePath +: args : _*)

  def ghc(envConfigurer: OptEnvConfigurer, args: String*): Unit =
    exec(
      None,
      envConfigurer,
      "stack ghc --",
      args : _*)

  def ghcPkgRecache(envConfigurer: OptEnvConfigurer, ghcPkgPath: String, sandbox: Sandbox): Unit =
    exec(
      None,
      envConfigurer,
      ghcPkgPath,
      "-f",
      sandbox.packageDb.getAbsolutePath,
      "recache")

  def ghcPkgList(envConfigurer: OptEnvConfigurer, sandboxes: List[Sandbox]): Unit =
    exec(
      None,
      envConfigurer,
      "ghc-pkg",
      "list" :: sandboxes.map(_.asPackageDbArg) : _*)

  def getCabalVersion(envConfigurer: OptEnvConfigurer, ghcPkgPath: String): String = {
    val output = capturedExec(
      None,
      envConfigurer,
      ghcPkgPath,
      "describe", "Cabal"
    )

    output
      .split('\n')
      .filter(_.startsWith("version: "))
      .map(_.substring("version: ".length))
      .head
  }

  def stack(stackRoot: Option[String], envConfigurer: OptEnvConfigurer, workingDir: File, params: String*): Unit =
    exec(
      Some(workingDir),
      setStackRoot(envConfigurer, stackRoot),
      "stack",
      params : _*
    )

  def capturedStack(stackRoot: Option[String], envConfigurer: OptEnvConfigurer, workingDir: File, params: String*): String =
    capturedExec(
      Some(workingDir),
      setStackRoot(envConfigurer, stackRoot),
      "stack",
      params : _*
    )

  private def setStackRoot(envConfigurer: OptEnvConfigurer, stackRoot: Option[String]): OptEnvConfigurer = {
    val fn: java.util.Map[String, AnyRef] => Unit = env => {
      envConfigurer match {
        case Some(inner) => inner.call(env)
        case None =>
      }

      stackRoot match {
        case Some(path) =>
          env.put("STACK_ROOT", path)
        case None =>
      }
    }

    Some(asClosure[AnyRef] { o => fn(o.asInstanceOf[java.util.Map[String, AnyRef]]) })
  }

  private def profilingArgs(profiling: Boolean, cabalVersion: CabalVersion): List[String] = {
    if (profiling) {
      List(
        "--enable-executable-profiling",
        "--enable-library-profiling"
      )
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

  class CabalContext(
    val version: CabalVersion,
    val root: File,
    getTargetSandbox: => Sandbox,      // not every cabal command require these, so we evaluate them lazily
    getDependencies: => List[Sandbox],
    val profiling: Boolean,
    val configFile: Option[String],
    val envConfigurer: OptEnvConfigurer) {

    lazy val targetSandbox = getTargetSandbox
    lazy val dependencies = getDependencies
  }
}