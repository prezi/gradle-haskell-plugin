package com.prezi.haskell.gradle

object Profiling {

  def measureTime[T](fn : => T): (T, Double) = {
    val t0 = System.nanoTime()
    val result = fn
    val t1 = System.nanoTime()
    (result, (t1 - t0) * 1.0e-9)
  }
}
