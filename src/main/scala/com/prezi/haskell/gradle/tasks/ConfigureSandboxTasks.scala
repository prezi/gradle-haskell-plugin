package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters._

/**
 * Configures the @ExtractDependentSandboxes task's inputs and outputs
 */
class ConfigureSandboxTasks extends DefaultTask with HaskellDependencies {

  var extractTask: Option[ExtractDependentSandboxes] = None
  var fixTask: Option[FixDependentSandboxes] = None

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet

    if (!extractTask.isDefined) {
      throw new IllegalStateException("extractTask is not set")
    }

    if (!fixTask.isDefined) {
      throw new IllegalStateException("fixTask is not set")
    }

    for (artifact <- configuration.get.getResolvedConfiguration.getResolvedArtifacts.asScala) {
      val sandbox = Sandbox.fromResolvedArtifact(getProject, artifact)

      extractTask.get.getInputs.file(artifact.getFile)
      extractTask.get.getOutputs.dir(sandbox.extractionRoot)

      fixTask.get.getInputs.dir(sandbox.extractionRoot)
      fixTask.get.getOutputs.dir(sandbox.root)
    }
  }
}
