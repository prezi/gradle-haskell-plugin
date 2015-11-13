package com.prezi.haskell.gradle.io.packers

import java.io.File

/**
  * Abstract zip unpacker interface
  */
trait Unpacker {
  def unpack(zipFile: File, targetDir: File): Unit
}
