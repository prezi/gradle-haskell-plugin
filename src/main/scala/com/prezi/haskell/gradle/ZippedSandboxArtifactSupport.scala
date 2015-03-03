package com.prezi.haskell.gradle

import com.prezi.haskell.gradle.tasks.ZippedSandbox
import org.gradle.api.Project

/**
 * Adds the project's sandbox as an artifact of the project
 */
class ZippedSandboxArtifactSupport(protected val project: Project) extends ZippedSandboxArtifactSupportImpl with ProjectExtender {

  defineZipSandboxArtifact
}

trait ZippedSandboxArtifactSupportImpl {
  this: ProjectExtender =>

  protected def defineZipSandboxArtifact(): Unit = {
    val zipSandbox = project.getTasks.create("zipSandbox", classOf[ZippedSandbox])
    project.getArtifacts.add("main", zipSandbox)
  }
}
