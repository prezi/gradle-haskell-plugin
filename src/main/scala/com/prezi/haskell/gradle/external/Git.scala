package com.prezi.haskell.gradle.external

import java.io.File
import java.net.URL

import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

class Git(executor : Action[ExecSpec] => ExecResult)
  extends ToolsBase(executor) {

  def clone(repoUrl: URL, targetDir: File): Unit = {
    exec(None, None, "git", "clone", repoUrl.toString, targetDir.getAbsolutePath)
  }
}