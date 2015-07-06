package com.prezi.haskell.gradle.model

import java.io.{FileInputStream, File}

import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FilenameUtils._
import resource._

case class SandboxArtifact(name: String, artifact: File) {

  private def calculateChecksum: String =
    (managed(new FileInputStream(artifact)) map DigestUtils.md5Hex).opt.get


  def toSandbox(root: File): Sandbox =
    new Sandbox(root </> "deps" </> getBaseName(name) </> calculateChecksum)

}
