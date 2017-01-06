package com.prezi.haskell.gradle.external

import java.io.{File, FileInputStream}

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.ToolsBase.OptEnvConfigurer
import com.prezi.haskell.gradle.model.Sandbox
import org.apache.commons.codec.digest.DigestUtils
import resource._

class SandFix(sandFixPath: File, haskellTools: HaskellTools) {
  private val sourceHash = calculateSourceHash()

  def run(envConfigurer: OptEnvConfigurer, sandbox: Sandbox, others: List[Sandbox], stackRoot: Option[String]): Unit = {

    haskellTools.stack(stackRoot, envConfigurer, None, "setup")
    val cabalVersion = haskellTools.getCabalVersion(stackRoot, envConfigurer)
    val cacheDir = getCacheDir(cabalVersion, sourceHash)
    val cachedSandfix = getCachedFile(cacheDir)

    if (!cachedSandfix.exists()) {
      compileToCache(envConfigurer, cacheDir)
    }

    val dbArgs = others.map(child => child.asPackageDbArg)
    val args = List(
      "exec",
      cachedSandfix.getAbsolutePath,
      "--",
      sandbox.root.getAbsolutePath,
      sandbox.packageDb.getName,
      "--package-db=global") ::: dbArgs.toList

    haskellTools.stack(stackRoot, envConfigurer, None, args : _*)
  }

  private def calculateSourceHash(): String =
    (managed(new FileInputStream(sandFixPath)) map DigestUtils.md5Hex).opt.get

  private def getCacheDir(cabalVersion: String, hash: String): File =
    new File(System.getProperty("user.home")) </> ".gradle-haskell" </> "sandfix-cache" </> s"$cabalVersion-$hash"

  private def compileToCache(envConfigurer: OptEnvConfigurer, cacheDir: File): Unit = {
    if (!cacheDir.exists()) {
      cacheDir.mkdirs()
    }
    haskellTools.ghc(envConfigurer, "-O2", "-o", getCachedFile(cacheDir).getAbsolutePath, sandFixPath.getAbsolutePath)
  }

  private def getCachedFile(cacheDir: File): File = cacheDir </> "sandfix"
}
