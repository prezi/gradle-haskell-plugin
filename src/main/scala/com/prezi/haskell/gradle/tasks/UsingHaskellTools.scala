package com.prezi.haskell.gradle.tasks

import java.io.File

import com.prezi.haskell.gradle.external.HaskellTools
import org.gradle.api.{GradleException, Project, Task}

import scala.io.Source

/**
  * Mixin for tasks requiring a @HaskellTools instance to work with
  */
trait UsingHaskellTools {
  this: Task =>

  var tools: Option[HaskellTools] = None

  protected def needsToolsSet: Unit = {
    if (!tools.isDefined) {
      throw new IllegalStateException("tools is not specified")
    }
  }

  protected def ghcPkgPath(project: Project): String = {
    val pathCache = StackPathTask.getPathCache(project)
    if (!pathCache.exists) {
      throw new GradleException(s"Stack path cache (${pathCache.getAbsolutePath}) does not exists")
    }

    val key = "compiler-bin: "
    val compilerBin = Source
      .fromFile(pathCache)
      .getLines()
      .find(_.startsWith(key))
      .map(_.substring(key.length))

    compilerBin match {
      case Some(path) => s"$path/ghc-pkg"
      case None => throw new GradleException(s"Invalid 'stack path' output (${pathCache.getAbsolutePath})")
    }
  }
}