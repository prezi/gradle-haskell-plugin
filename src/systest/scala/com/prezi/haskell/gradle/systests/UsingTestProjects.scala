package com.prezi.haskell.gradle.systests

import java.io.{InputStreamReader, BufferedReader, File}

import com.google.common.io.Files
import org.apache.commons.io.FileUtils

trait UsingTestProjects {

  private val testProjectsDirProp = System.getProperty("test-projects-dir")
  private val pluginBuildDirProp = System.getProperty("plugin-build-dir")

  protected def withCleanWorkingDir[R](action: File => R): R = {
    val tempDir = Files.createTempDir()
    try {
      println(s"Using temporary directory ${tempDir.getAbsolutePath}")
      FileUtils.copyDirectory(new File(testProjectsDirProp), tempDir)

      action(tempDir)
    }
    finally {
      //FileUtils.deleteDirectory(tempDir)
    }
  }

  protected def buildGradleExists(path: File): Boolean = {
    println(s"Testing ${new File(path, "build.gradle").getAbsolutePath}'s existance")
    new File(path, "build.gradle").exists()
  }

  protected def gradle(root: File, args: String*): Boolean = {
    val process = new ProcessBuilder()
      .inheritIO()
      .command("gradle" +: "--no-daemon" +: s"-PpluginBuildDir=$pluginBuildDirProp" +: args : _*)
      .directory(root)
      .start()
    process.waitFor() == 0
  }

  protected def runApp(root: File): String = {
    val process = new ProcessBuilder()
      .command(appOutput(root).getAbsolutePath)
      .start()

    val reader = new BufferedReader(new InputStreamReader(process.getInputStream))
    val result = reader.readLine()
    process.waitFor()
    result
  }

  protected def appOutputExists(path: File): Boolean =
    appOutput(path).exists()

  protected def appOutput(path: File): File =
    new File(new File(new File(new File(new File(new File(path, "app"), "build"), "sandbox"), "files"), "bin"), "app")
}
