package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.Task

trait UsesSandbox {
  this: Task with HaskellProjectSupport =>

  if (haskellExtension.getUseStack) {
    dependsOn("generateStackYaml")
  }

  protected def sandbox: Sandbox =
    getProject.getExtensions.getByType(classOf[Sandbox])
}
