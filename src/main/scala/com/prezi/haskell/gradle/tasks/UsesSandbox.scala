package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import java.io.File
import org.gradle.api.Task

trait UsesSandbox {
  this: Task with HaskellProjectSupport =>

  if (haskellExtension.getUseStack) {
    dependsOn("stackPath")
  }

  protected lazy val sandbox: Sandbox =
    Sandbox.createForProject(getProject, haskellExtension.getUseStack)

  protected lazy val configTimeSandboxRoot: File =
    if (haskellExtension.getUseStack) {
      getProject.getProjectDir </> ".stack-work"
    } else {
      sandbox.root
    }
}
