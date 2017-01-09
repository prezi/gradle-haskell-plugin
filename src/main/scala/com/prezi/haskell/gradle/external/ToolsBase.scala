package com.prezi.haskell.gradle.external

import java.io.{ByteArrayOutputStream, File}

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

class ToolsBase(executor : Action[ExecSpec] => ExecResult) {
  protected def exec(workDir: Option[File], extraEnvironmentVariables: Map[String, String], program: String, args: String*): Unit = {
    executor(asAction({ spec: ExecSpec =>
      val cmdLine = getCmdLine(workDir, program, args)
      spec.commandLine(cmdLine: _*)

      workDir map { spec.workingDir(_) }
      extraEnvironmentVariables.foreach { case (key, value) =>
          spec.environment(key, value)
      }
    }))
  }

  protected def capturedExec(workDir: Option[File], extraEnvironmentVariables: Map[String, String], program: String, args: String*): String = {
    val stream = new ByteArrayOutputStream()

    executor(asAction({ spec: ExecSpec =>
      val cmdLine = getCmdLine(workDir, program, args)
      spec.commandLine(cmdLine: _*)

      workDir map { spec.workingDir(_) }
      spec.setStandardOutput(stream)
      extraEnvironmentVariables.foreach { case (key, value) =>
        spec.environment(key, value)
      }
    }))

    stream.toString
  }

  private def getCmdLine(workDir: Option[File], program: String, args: Seq[String]): Seq[String] = {
      program +: args
  }
}
