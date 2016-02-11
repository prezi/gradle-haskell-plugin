package com.prezi.haskell.gradle.external

import java.io.File
import java.net.URL

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.ToolsBase.OptEnvConfigurer
import org.gradle.api.{GradleException, Action}
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Wraps the `snapshot-versions` tool which lists all the dependencies read from
 * a cabal file with their corresponding versions fetched from a stackage snapshot.
 */
class SnapshotVersions(isOffline: Boolean, overriddenCacheDir: Option[File], envConfigurer: OptEnvConfigurer, executor : Action[ExecSpec] => ExecResult, haskellTools: HaskellTools, git: Git) {

  def run(snapshot: String, cabal: File): Array[String] = {
    ensureToolExists()
    val output = haskellTools.capturedStack(envConfigurer, cacheDir, "exec", "snapshot-versions", "--", cabal.getAbsolutePath, snapshot, "--stack-yaml")

    output.split('\n').map(_.trim).filter(_.length > 0)
  }

  private def ensureToolExists(): Unit = {
    if (!cacheDir.exists) {
      fetchSource()
      buildSource()
    }
    else if (!isOffline) {
      updateSource()
      buildSource()
    }
  }

  private val cacheDir: File =
    overriddenCacheDir.getOrElse(
      new File(System.getProperty("user.home")) </> ".gradle-haskell" </> "snapshot-versions")

  private def fetchSource(): Unit = {
    if (!isOffline) {
      git.clone(new URL("https://github.com/vigoo/snapshot-versions.git"), cacheDir)
    }
    else {
      throw new GradleException("Cannot clone snapshot-versions in offline mode!")
    }
  }

  private def updateSource(): Unit = {
    if (!isOffline) {
      git.pull(cacheDir)
    }
    else {
      throw new GradleException("Cannot pull snapshot-versions in offline mode!")
    }
  }

  private def buildSource(): Unit = {
    haskellTools.stack(envConfigurer, cacheDir, "setup")
    haskellTools.stack(envConfigurer, cacheDir, "build")
  }
}
