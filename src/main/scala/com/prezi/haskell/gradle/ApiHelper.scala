package com.prezi.haskell.gradle

import java.io.File

import groovy.lang.Closure
import org.gradle.api.{Project, Action}
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.component.{ProjectComponentIdentifier, ModuleComponentIdentifier, ComponentIdentifier}
import org.gradle.internal.reflect.Instantiator

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
 * Helper functions for better interop with Gradle and/or Groovy
 */
object ApiHelper {

  implicit def asAction[T](action: T => Unit): Action[T] =
    new Action[T] {
      override def execute(t: T): Unit = action(t)
    }

  implicit def asClosure[T](fun: T => Unit): Closure[T] =
    new Closure[T](()) {
      protected def doCall(args: AnyRef): AnyRef = {
        fun(args.asInstanceOf[T])
        null
      }
    }

  implicit def asClosureWithReturn[A, B<:AnyRef](fun: A => B): Closure[A] =
    new Closure[A](()) {
      protected def doCall(args: AnyRef): AnyRef = {
        fun(args.asInstanceOf[A])
      }
    }

  implicit def instantiatorExt(instantiator: Instantiator): InstantiatorExt =
    new InstantiatorExt(instantiator)

  class InstantiatorExt(instantiator: Instantiator) {
    def create[T](params: Object*)(implicit t: ClassTag[T]): T = {
      instantiator.newInstance[T](t.runtimeClass.asInstanceOf[Class[T]], params : _*)
    }
  }

  implicit def fileExt(file: File): FileExt = new FileExt(file)

  class FileExt(file: File) {
    def </> (subPath: String): File = new File(file, subPath)
  }
}

case class ModuleId(group: String, name: String, version: String) {
  def toDisplayName: String =
    s"$group-$name-$version"
}

case object ModuleId {
  def fromModuleVersionIdentifier(id: ModuleVersionIdentifier): ModuleId =
    ModuleId(id.getGroup, id.getName, id.getVersion)

  def fromComponentIdentifier(rootProject: Project, id: ComponentIdentifier): ModuleId = id match {
    case mcid: ModuleComponentIdentifier =>
      ModuleId(mcid.getGroup, mcid.getModule, mcid.getVersion)
    case pcid: ProjectComponentIdentifier =>
      val project = rootProject.findProject(pcid.getProjectPath)
      ModuleId(project.getGroup.asInstanceOf[String], project.getName, project.getVersion.asInstanceOf[String])
  }
}
