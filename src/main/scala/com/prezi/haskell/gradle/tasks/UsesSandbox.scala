package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.Sandbox
import java.io.File
import org.gradle.api.Task

trait UsesSandbox {
  this: Task with HaskellProjectSupport =>

  dependsOn("stackPath")

  protected lazy val sandbox: Sandbox =
    Sandbox.createForProject(getProject)

  protected lazy val configTimeSandboxRoot: File =
    getProject.getProjectDir </> ".stack-work" </> "install"
}
