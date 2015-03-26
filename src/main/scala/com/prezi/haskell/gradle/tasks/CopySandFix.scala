package com.prezi.haskell.gradle.tasks

import java.io._

import com.prezi.haskell.gradle.ApiHelper._
import org.apache.commons.io.IOUtils

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import resource._

/**
 * Extracts the SandFix from the plugin's resources
 */
class CopySandFix extends DefaultTask {

  var sandFixDir: Option[File] = None

  @TaskAction
  def run(): Unit = {
    if (!sandFixDir.isDefined) {
      throw new IllegalStateException("sandFixDir is not specified")
    }

    sandFixDir.get.mkdirs()
    val sourceUrl = getClass.getClassLoader.getResource("com/prezi/haskell/gradle/tasks/SandFix.hs")

    for (src <- managed(sourceUrl.openStream());
         dest <- managed(new FileOutputStream(sandFixDir.get </> "SandFix.hs"))) {
      IOUtils.copy(src, dest)
    }
  }
}
