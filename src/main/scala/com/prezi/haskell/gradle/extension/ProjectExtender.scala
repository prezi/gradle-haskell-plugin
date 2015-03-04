package com.prezi.haskell.gradle.extension

import org.gradle.api.{Task, Project}
import org.gradle.api.artifacts.Configuration

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

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

  def createTask[T <: Task](name: String)(implicit t: ClassTag[T]): T = {
    project.getTasks.create(name, t.runtimeClass.asInstanceOf[Class[T]])
  }

  def getTask[T <: Task](name: String): T = {
    project.getTasks.getByName(name).asInstanceOf[T]
  }

  def isTaskDefined(name: String): Boolean = {
    project.getTasks.findByName(name) != null
  }

  def createField[T](name: String, params: Object*)(implicit t: ClassTag[T]): Unit = {
    project.getExtensions.create(name, t.runtimeClass.asInstanceOf[Class[T]], params : _*)
  }
}