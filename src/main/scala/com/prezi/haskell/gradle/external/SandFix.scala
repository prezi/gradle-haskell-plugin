package com.prezi.haskell.gradle.external

import java.io.{FileInputStream, File}

import com.prezi.haskell.gradle.external.HaskellTools.OptEnvConfigurer
import com.prezi.haskell.gradle.model.Sandbox
import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}
import resource._

class SandFix(executor : Action[ExecSpec] => ExecResult, sandFixPath: File, haskellTools: HaskellTools) {
  private val sourceHash = calculateSourceHash()

  def run(envConfigurer: OptEnvConfigurer, sandbox: Sandbox, others: List[Sandbox]): Unit = {

    val cabalVersion = haskellTools.getCabalVersion(envConfigurer)
    val cacheDir = getCacheDir(cabalVersion, sourceHash)
    val cachedSandfix = getCachedFile(cacheDir)

    if (!cachedSandfix.exists()) {
      compileToCache(envConfigurer, cacheDir)
    }

    val dbArgs = others.map(child => child.asPackageDbArg)
    val args = List(
      sandbox.root.getAbsolutePath,
      "packages",
      "--package-db=global") ::: dbArgs.toList

    executor(asAction({ spec: ExecSpec =>
      // Wrapping commands in "sh" if needed so they can use the
      // new PATH created by the envConfigurer
      val cmdLine: Seq[String] = envConfigurer match {
        case Some(_) => Seq("sh", "-c", (cachedSandfix.getAbsolutePath :: args).mkString(" "))
        case None => cachedSandfix.getAbsolutePath +: args.toSeq
      }

      spec.commandLine(cmdLine : _*)
      envConfigurer map { _.call(spec.getEnvironment()) }
    }))
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
