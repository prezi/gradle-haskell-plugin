package com.prezi.haskell.gradle.io.packers

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.Project
import org.gradle.api.file.CopySpec

/**
  * ZIP unpacker implementation based on Gradle
  */
class GradleZipPacker(project: Project) extends Unpacker {

  override def unpack(zipFile: File, targetDir: File): Unit = {
    project.copy(asClosure { spec: CopySpec =>
      spec.from(project.zipTree(zipFile))
      spec.into(targetDir)
    })
  }
}
