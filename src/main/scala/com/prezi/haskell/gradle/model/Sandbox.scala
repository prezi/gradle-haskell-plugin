package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.io.FilenameUtils._
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact

/**
 * Represents a custom sandbox
 * @param root Root directory of the sandbox
 */
class Sandbox(val root: File) {

  val packageDb = root </> "packages"
  val installPrefix = root </> "files"

  def asPackageDbArg: String = s"--package-db=$packageDb"
  def asPrefixArg: String = s"--prefix=$installPrefix"

  override def toString = root.getAbsolutePath
}

object Sandbox {
  def fromResolvedArtifact(project: Project, artifact: ResolvedArtifact): Sandbox =
    new Sandbox(project.getBuildDir </> "deps" </> getBaseName(artifact.getFile.getName))
}