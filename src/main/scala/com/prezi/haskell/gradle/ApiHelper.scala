package com.prezi.haskell.gradle

import groovy.lang.Closure
import org.gradle.api.Action

import scala.language.implicitConversions

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
}
