package com.prezi.haskell.gradle.model

import java.io.{FileInputStream, File}

import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FilenameUtils._
import resource._

case class SandboxArtifact(name: String, artifact: File) {

  private def calculateChecksum: String =
    (managed(new FileInputStream(artifact)) map DigestUtils.md5Hex).opt.get


  def toCabalSandbox(root: File, useStack: Boolean): Sandbox =
    new CabalSandbox(root </> "deps" </> getBaseName(name) </> calculateChecksum)

  def toStackSandbox(root: File, useStack: Boolean): Sandbox =
    new StackSandbox(root </> "deps" </> getBaseName(name) </> calculateChecksum)

  def toNormalizedString = name + "#" + artifact.getAbsoluteFile

  private def depsName(useStack: Boolean): String =
    useStack match {
      case true => "deps-stack"
      case false => "deps"
    }
}
