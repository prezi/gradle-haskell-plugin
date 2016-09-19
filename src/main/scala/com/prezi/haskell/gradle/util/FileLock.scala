package com.prezi.haskell.gradle.util

import java.io.File

import scala.util.Random

class FileLock(file: File) {
  def lock(): Unit = {
    val random = new Random()
    file.getParentFile.mkdirs()
    // According to the javadoc, java.io.File.createNewFile should not be used for locking, but for this non-critical
    // use-case it is a good enough solution.
    while (!file.createNewFile())
      Thread.sleep(random.nextInt(100))
  }

  def release(): Unit = {
    file.delete()
  }
}
