package com.prezi.haskell.gradle.model

import java.io.{FileInputStream, File}

import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FilenameUtils._
import resource._

case class SandboxArtifact(name: String, artifact: File) {

  private def calculateChecksum: String =
    (managed(new FileInputStream(artifact)) map DigestUtils.md5Hex).opt.get

  private val depsCabal = "deps"
  private val depsStack = "deps-stack"

  def toCabalSandbox(root: File): Sandbox =
    new CabalSandbox(root </> depsCabal </> getBaseName(name) </> calculateChecksum)

  def toStackSandbox(root: File): Sandbox =
    new StackSandbox(root </> depsStack </> getBaseName(name) </> calculateChecksum)

  def toNormalizedString = name + "#" + artifact.getAbsoluteFile
}
