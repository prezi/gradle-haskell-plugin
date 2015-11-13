package com.prezi.haskell.gradle.tasks

import org.gradle.api.Task
import org.gradle.api.logging.LogLevel

trait TaskLogging {
  this: Task =>

  def debug(msg: String) =
    getLogger.log(LogLevel.DEBUG, msg)

  def info(msg: String) =
    getLogger.log(LogLevel.INFO, msg)
}
