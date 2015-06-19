package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._

/**
 * Represents a custom sandbox
 * @param root Root directory of the sandbox
 */
class Sandbox(val root: File) {

  val extractionRoot = root
  val packageDb = root </> "packages"
  val installPrefix = root </> "files"
  val lock = root </> ".lock"

  def asPackageDbArg: String = s"--package-db=$packageDb"
  def asPrefixArg: String = s"--prefix=$installPrefix"

  override def toString = root.getAbsolutePath
}
