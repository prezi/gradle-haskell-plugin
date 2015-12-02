package com.prezi.haskell.gradle.systests

import java.io.File
import com.prezi.haskell.gradle.ApiHelper._

trait UsingPride {
  this: UsingTestProjects =>

  protected def initPride(root: File): Unit = {
    val process = new ProcessBuilder()
      .command("pride", "init", "--force", "--gradle-version", "2.4")
      .directory(root)
      .start()

    StreamToStdout(process.getErrorStream)
    StreamToStdout(process.getInputStream)

    process.waitFor() == 0
  }

  protected def initWorkspace(root: File): Unit = {
    hardcodePluginBuildDir (root </> "lib1" </> "build.gradle")
    hardcodePluginBuildDir (root </> "lib2" </> "build.gradle")
    hardcodePluginBuildDir (root </> "app" </> "build.gradle")
    initPride (root)
  }
}
