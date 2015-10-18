package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.Project

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
      new StackSandbox(project.getProjectDir </> ".stack-work") // TODO
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