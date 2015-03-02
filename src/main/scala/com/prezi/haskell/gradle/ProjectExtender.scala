package com.prezi.haskell.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import scala.collection.JavaConverters._

/**
 * Helper functions for extending a project with new fields, configurations, etc.
 */
trait ProjectExtender {
  protected def project: Project

  def addConfiguration(configuration: String): Configuration = {
    val configs = project.getConfigurations
    val config = configs.create(configuration)
    configs.add(config)
    config
  }

  def addConfigurations(configurations: String*): Unit = {
    val configs = project.getConfigurations

    configs.addAll(configurations.map(configs.create).asJavaCollection)
  }

  def getConfiguration(name: String): Configuration = {
    val configs = project.getConfigurations
    configs.findByName(name)
  }

  def addField[T](name: String, value: T): Unit = {
    project.getExtensions.add(name, value)
  }

  def getField[T](name: String): T = {
    project.getExtensions.getByName(name).asInstanceOf[T]
  }
}