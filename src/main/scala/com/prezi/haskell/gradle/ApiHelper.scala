package com.prezi.haskell.gradle

import java.io.File

import groovy.lang.Closure
import org.gradle.api.Action
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

  implicit def asClosure[T](action: T => Unit): Closure[T] =
    new Closure[T](()) {
      protected def doCall(args: AnyRef): AnyRef = {
        action(args.asInstanceOf[T])
        null
      }
    }

  implicit def instantiatorExt(instantiator: Instantiator) =
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
