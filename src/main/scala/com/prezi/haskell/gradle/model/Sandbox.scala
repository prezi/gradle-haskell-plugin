package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.tasks.StackPathTask
import org.gradle.api.{GradleException, Project}

import scala.io.Source

abstract class Sandbox(val root: File) {

  val extractionRoot: File = root

  def packageDb: File

  def installPrefix: File

  val lock: File = root </> ".lock"

  def asPackageDbArg: String = s"--package-db=$packageDb"

  def asPrefixArg: String = s"--prefix=$installPrefix"

  override def toString: String = root.getAbsolutePath
}

object Sandbox {
  def createForProject(project: Project): Sandbox = {
    val pathCache = StackPathTask.getPathCache(project)
    if (!pathCache.exists) {
      throw new GradleException(s"Stack path cache (${pathCache.getAbsolutePath}) does not exists")
    }

    val key = "local-install-root: "
    val localInstallRoot = Source
      .fromFile(pathCache)
      .getLines()
      .find(_.startsWith(key))
      .map(_.substring(key.length))

    localInstallRoot match {
      case Some(path) => new StackSandbox(new File(path))
      case None => throw new GradleException(s"Invalid 'stack path' output (${pathCache.getAbsolutePath})")
    }
  }

}

class StackSandbox(root: File)
  extends Sandbox(root) {

  val packageDb: File = root </> "pkgdb"
  val installPrefix: File = root
}