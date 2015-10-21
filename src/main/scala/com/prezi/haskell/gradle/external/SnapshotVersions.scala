package com.prezi.haskell.gradle.external

import java.io.File
import java.net.URL

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.ToolsBase.OptEnvConfigurer
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Wraps the `snapshot-versions` tool which lists all the dependencies read from
 * a cabal file with their corresponding versions fetched from a stackage snapshot.
 */
class SnapshotVersions(envConfigurer: OptEnvConfigurer, executor : Action[ExecSpec] => ExecResult, haskellTools: HaskellTools, git: Git) {

  def run(snapshot: String, cabal: File): Array[String] = {
    ensureToolExists()
    val output = haskellTools.capturedStack(envConfigurer, cacheDir, "exec", "snapshot-versions", "--", cabal.getAbsolutePath, snapshot, "--stack-yaml")

    output.split('\n')
  }

  private def ensureToolExists(): Unit = {
    if (!cacheDir.exists) {
      fetchSource(cacheDir)
      buildSource(cacheDir)
    }

    // TODO: update if not in offline mode
  }

  private val cacheDir: File =
    new File(System.getProperty("user.home")) </> ".gradle-haskell" </> "snapshot-versions"

  private def fetchSource(root: File): Unit = {
    git.clone(new URL("https://github.com/vigoo/snapshot-versions.git"), cacheDir)
  }

  private def buildSource(root: File): Unit = {
    haskellTools.stack(envConfigurer, cacheDir, "setup")
    haskellTools.stack(envConfigurer, cacheDir, "build")
  }
}
