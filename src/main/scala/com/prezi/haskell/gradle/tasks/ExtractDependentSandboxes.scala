package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

/**
 * Extracts a project's dependencies as custom sandboxes
 */
class ExtractDependentSandboxes extends DefaultTask with HaskellDependencies {

  override def onConfigurationSet(cfg: Configuration): Unit = {
    dependsOn(cfg)
  }

  @TaskAction
  def run(): Unit = {
    needsConfigurationSet

    for (artifact <- configuration.get.getResolvedConfiguration.getResolvedArtifacts.asScala) {
      val sandbox = Sandbox.fromResolvedArtifact(getProject, artifact)

      getLogger.trace("Extracting sandbox dependency {}", artifact.getFile.getName)

      sandbox.root.mkdirs()
      getProject.copy(asClosure { spec : CopySpec =>
        spec.from (getProject.zipTree(artifact.getFile))
        spec.into (sandbox.root)
      })
    }
  }
}
