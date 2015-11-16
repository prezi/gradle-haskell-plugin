package com.prezi.haskell.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

/**
 * Configures the @StoreDependentSandboxes task's inputs and outputs
 */
class ConfigureSandboxTasks
  extends DefaultTask
  with HaskellDependencies
  with TaskLogging {

  var storeTask: Option[StoreDependentSandboxes] = None

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet

    if (storeTask.isEmpty) {
      throw new IllegalStateException("extractTask is not set")
    }

    for (artifact <- configuration.get.getResolvedConfiguration.getResolvedArtifacts.asScala) {
      storeTask.get.getInputs.file(artifact.getFile)

      debug(s"Adding ${artifact.getFile} as input for $storeTask")
    }
  }
}
