package com.prezi.haskell.gradle.external

import java.io.File

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Executes external tools with the Gradle project's execution command
  *
  * @param executor The `project.exec` function
 */
class HaskellTools(executor : Action[ExecSpec] => ExecResult, stackToolsDir: => File)
  extends ToolsBase(executor) {

  def ghc(stackRoot: Option[String], args: String*): Unit =
    stack(
      stackRoot,
      None,
      "ghc" +: "--" +: args : _*)

  def ghcPkgRecache(stackRoot: Option[String], sandbox: Sandbox): Unit =
    stack(
      stackRoot,
      None,
      "exec",
      "ghc-pkg",
      "--",
      "-f",
      sandbox.packageDb.getAbsolutePath,
      "recache")

  def ghcPkgList(stackRoot: Option[String], sandboxes: List[Sandbox]): Unit =
    stack(
      stackRoot,
      None,
      "ghc-pkg" +:  "-- " +: "list" +: sandboxes.map(_.asPackageDbArg) : _*)

  def getCabalVersion(stackRoot: Option[String]): String = {
    val output = capturedStack(
      stackRoot,
      None,
      "exec",
      "ghc-pkg",
      "--",
      "describe", "Cabal"
    )

    output
      .split('\n')
      .filter(_.startsWith("version: "))
      .map(_.substring("version: ".length))
      .head
  }

  def stack(stackRoot: Option[String], workingDir: Option[File], params: String*): Unit =
    exec(
      workingDir.orElse(Some(stackToolsDir)),
      setStackRoot(stackRoot),
      "stack",
      params : _*
    )

  def capturedStack(stackRoot: Option[String], workingDir: Option[File], params: String*): String =
    capturedExec(
      workingDir.orElse(Some(stackToolsDir)),
      setStackRoot(stackRoot),
      "stack",
      params : _*
    )

  private def setStackRoot(stackRoot: Option[String]): Map[String, String] = {
    stackRoot match {
      case Some(root) => Map("STACK_ROOT" -> root)
      case None => Map.empty
    }
  }

  private def profilingArgs(profiling: Boolean): List[String] = {
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
