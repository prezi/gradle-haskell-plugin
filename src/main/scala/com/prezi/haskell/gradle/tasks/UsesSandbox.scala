package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.Task

trait UsesSandbox {
  this: Task with HaskellProjectSupport =>

  if (haskellExtension.getUseStack) {
    dependsOn("stackPath")
  }

  protected def sandbox: Sandbox =
    Sandbox.createForProject(getProject, haskellExtension.getUseStack)
}
