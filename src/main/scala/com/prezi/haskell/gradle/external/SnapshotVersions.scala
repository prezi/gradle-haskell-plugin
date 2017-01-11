package com.prezi.haskell.gradle.external

import java.io.File
import java.net.URL

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.{Action, GradleException}
import org.gradle.process.{ExecResult, ExecSpec}

/**
 * Wraps the `snapshot-versions` tool which lists all the dependencies read from
 * a cabal file with their corresponding versions fetched from a stackage snapshot.
 */
class SnapshotVersions(isOffline: Boolean, overriddenCacheDir: Option[File], stackRoot: Option[String], executor : Action[ExecSpec] => ExecResult, haskellTools: HaskellTools, git: Git) {

  def run(snapshot: String, cabal: File): Array[String] = {
    ensureToolExists()
    val output = haskellTools.capturedStack(stackRoot, Some(cacheDir), "exec", "snapshot-versions", "--", cabal.getAbsolutePath, snapshot, "--stack-yaml")

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
    // NOTE: while using the temporary backported Sierra fix, it is not safe to use the
    // stack executor here, as it installs the non-patched GHC to global. Replace this
    // back once GHC 8.0.2 is out:
    // haskellTools.stack(stackRoot, Some(cacheDir), "build")
    haskellTools.unsafeStack(stackRoot, Some(cacheDir), "build")
  }
}
