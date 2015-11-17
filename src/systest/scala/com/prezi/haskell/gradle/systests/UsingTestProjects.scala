package com.prezi.haskell.gradle.systests

import java.io.{PrintWriter, BufferedReader, File, InputStreamReader}

import com.google.common.io.Files
import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.io.FileUtils

import scala.io.Source

trait UsingTestProjects {

  private val testProjectsDirProp = System.getProperty("test-projects-dir")
  private val pluginBuildDirProp = System.getProperty("plugin-build-dir")

  protected def withCleanWorkingDir[R](testProjectName: String)(action: File => R): R = {
    val tempDir = Files.createTempDir()
    try {
      println(s"Using temporary directory ${tempDir.getAbsolutePath}")
      FileUtils.copyDirectory(new File(testProjectsDirProp) </> testProjectName, tempDir)

      action(tempDir)
    }
    finally {
      //FileUtils.deleteDirectory(tempDir)
    }
  }

  protected def buildGradleExists(path: File): Boolean = {
    println(s"Testing ${(path </> "build.gradle").getAbsolutePath}'s existance")
    (path </> "build.gradle").exists()
  }

  protected def gradle(root: File, args: String*): Boolean = {
    val process = new ProcessBuilder()
      .command("gradle" +: "--no-daemon" +: "--stacktrace" +: s"-PpluginBuildDir=$pluginBuildDirProp" +: args : _*)
      .directory(root)
      .start()

    StreamToStdout(process.getErrorStream)
    StreamToStdout(process.getInputStream)

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
    path </> "app" </> "build" </> "sandbox" </> "files" </> "bin" </> "app"

  protected def modifySource(path: File, regex: String, replacement: String): Unit = {
    val newContent = Source.fromFile(path).mkString.replaceAll(regex, replacement)
    val writer = new PrintWriter(path)
    try {
      writer.write(newContent)
    }
    finally {
      writer.close()
    }
  }

  protected def stackYamlExists(path: File): Boolean =
    (path </> "stack.yaml").exists()

  protected def stackYamlLines(path: File): List[String] =
    Source.fromFile(path </> "stack.yaml").getLines().toList
}
