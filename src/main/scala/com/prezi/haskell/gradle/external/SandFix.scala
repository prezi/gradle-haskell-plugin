package com.prezi.haskell.gradle.external

import java.io.{File, FileInputStream}

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import org.apache.commons.codec.digest.DigestUtils
import resource._

class SandFix(sandFixPath: File, haskellTools: HaskellTools) {
  private val sourceHash = calculateSourceHash()

  def run(sandbox: Sandbox, others: List[Sandbox], stackRoot: Option[String]): Unit = {
    val cabalVersion = haskellTools.getCabalVersion(stackRoot)
    val cacheDir = getCacheDir(cabalVersion, sourceHash)
    val cachedSandfix = getCachedFile(cacheDir)

    if (!cachedSandfix.exists()) {
      compileToCache(stackRoot, cacheDir)
    }

    val dbArgs = others.map(child => child.asPackageDbArg)
    val args = List(
      "exec",
      cachedSandfix.getAbsolutePath,
      "--",
      sandbox.root.getAbsolutePath,
      sandbox.packageDb.getName,
      "--package-db=global") ::: dbArgs

    haskellTools.stack(stackRoot, None, args : _*)
  }

  private def calculateSourceHash(): String =
    (managed(new FileInputStream(sandFixPath)) map DigestUtils.md5Hex).opt.get

  private def getCacheDir(cabalVersion: String, hash: String): File =
    new File(System.getProperty("user.home")) </> ".gradle-haskell" </> "sandfix-cache" </> s"$cabalVersion-$hash"

  private def compileToCache(stackRoot: Option[String], cacheDir: File): Unit = {
    if (!cacheDir.exists()) {
      cacheDir.mkdirs()
    }
    haskellTools.ghc(stackRoot, "-O2", "-o", getCachedFile(cacheDir).getAbsolutePath, sandFixPath.getAbsolutePath)
  }

  private def getCachedFile(cacheDir: File): File = cacheDir </> "sandfix"
}
