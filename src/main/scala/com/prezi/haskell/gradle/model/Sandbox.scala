package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.tasks.StackPathTask
import org.gradle.api.{GradleException, Project}

import scala.io.Source

abstract class Sandbox(val root: File) {

  val extractionRoot = root
  def packageDb: File
  def installPrefix: File
  val lock = root </> ".lock"

  def asPackageDbArg: String = s"--package-db=$packageDb"
  def asPrefixArg: String = s"--prefix=$installPrefix"

  override def toString = root.getAbsolutePath
}

object Sandbox {
  def createForProject(project: Project, useStack: Boolean): Sandbox = {
    if (useStack) {
      val pathCache = StackPathTask.getPathCache(project)
      if (!pathCache.exists) {
        throw new GradleException(s"Stack path cache (${pathCache.getAbsolutePath}) does not exists")
      }

      val localInstallRoot = Source
        .fromFile(pathCache)
        .getLines()
        .find(_.startsWith("local-install-root: "))

      localInstallRoot match {
        case Some(path) => new StackSandbox(new File(path))
        case None => throw new GradleException(s"Invalid 'stack path' output (${pathCache.getAbsolutePath})")
      }

    } else {
      new CabalSandbox(project.getBuildDir </> "sandbox")
    }
  }
}

/**
 * Represents a custom sandbox
 * @param root Root directory of the sandbox
 */
class CabalSandbox(root: File)
  extends Sandbox(root) {

  val packageDb = root </> "packages"
  val installPrefix = root </> "files"
}

class StackSandbox(root: File)
  extends Sandbox(root) {

  val packageDb = root </> "pkgdb"
  val installPrefix = root
}