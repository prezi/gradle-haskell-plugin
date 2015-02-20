package com.prezi.haskell.gradle.tasks

import java.io.File

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy

/**
 * Extracts the SandFix from the downloaded source artifact
 */
class CopySandFix extends Copy {

  def setUp(sandFixConfig: Configuration, sandFixDir: File): Unit = {
    getInputs files sandFixConfig
    into(sandFixDir)
    from(getProject().tarTree(sandFixConfig.getSingleFile))
  }
}
