package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.Profiling.measureTime
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

    val (artifacts, dt1) = measureTime { configuration.get.getResolvedConfiguration.getResolvedArtifacts.asScala }
    info(s"[PERFORMANCE] Getting resolved artifacts took $dt1 seconds")

    val (_, dt2) = measureTime {
      for (artifact <- artifacts) {
        storeTask.get.getInputs.file(artifact.getFile)

        debug(s"Adding ${artifact.getFile} as input for $storeTask")
      }
    }
    info(s"[PERFORMANCE] Setting storeTask's inputs took $dt2 seconds")
  }
}
