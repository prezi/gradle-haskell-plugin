package com.prezi.haskell.gradle.external

import java.io.{ByteArrayOutputStream, File}

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.external.ToolsBase.OptEnvConfigurer
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.process.{ExecResult, ExecSpec}

class ToolsBase(executor : Action[ExecSpec] => ExecResult) {
  protected def exec(workDir: Option[File], envConfigurer: OptEnvConfigurer, program: String, args: String*): Unit = {
    executor(asAction({ spec: ExecSpec =>
      val cmdLine = getCmdLine(workDir, envConfigurer, program, args)
      spec.commandLine(cmdLine: _*)

      envConfigurer map { _.call(spec.getEnvironment) }
      workDir map { spec.workingDir(_) }
    }))
  }

  protected def capturedExec(workDir: Option[File], envConfigurer: OptEnvConfigurer, program: String, args: String*): String = {
    val stream = new ByteArrayOutputStream()

    executor(asAction({ spec: ExecSpec =>
      val cmdLine = getCmdLine(workDir, envConfigurer, program, args)
      spec.commandLine(cmdLine: _*)

      envConfigurer map { _.call(spec.getEnvironment) }
      workDir map { spec.workingDir(_) }
      spec.setStandardOutput(stream)
    }))

    stream.toString
  }

  private def getCmdLine(workDir: Option[File], envConfigurer: OptEnvConfigurer, program: String, args: Seq[String]): Seq[String] = {
    // TODO: check for os?
    // Wrapping commands in "sh" if needed so they can use the
    // new PATH created by the envConfigurer
    envConfigurer match {
      case Some(_) => Seq("sh", "-c", (program :: args.toList).mkString(" "))
      case None => program +: args.toSeq
    }
  }
}

object ToolsBase {
  type EnvConfigurer = Closure[AnyRef]
  type OptEnvConfigurer = Option[Closure[AnyRef]]
}
