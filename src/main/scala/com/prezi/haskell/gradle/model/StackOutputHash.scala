package com.prezi.haskell.gradle.model

import java.io.{FileInputStream, FileOutputStream, PrintWriter, File}
import java.nio.file.Paths
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import resource._
import scala.collection.JavaConversions._
import scala.io.Source

class StackOutputSnapshot(private val data: Map[String, String]) {
  def differsFrom(other: StackOutputSnapshot) = {
    !(data equals other.data)
  }

  def saveSnapshot(file: File): Unit = {
    val writer = new PrintWriter(new FileOutputStream(file))
    try {
      for ((name, hash) <- data) {
        writer.print(name)
        writer.print(':')
        writer.print(hash)
        writer.println()
      }
    }
    finally {
      writer.close()
    }
  }
}

object StackOutputHash {
  def calculate(root: File): StackOutputSnapshot = {
    if (root.exists() && root.isDirectory) {
      val rootPath = Paths.get(root.getAbsolutePath)
      val pairs =
        for (file <- recursiveListFiles(root);
             name = rootPath.relativize(Paths.get(file.getAbsolutePath)).toString;
             hash = calculateHash(file)
        ) yield (name, hash)
      new StackOutputSnapshot(pairs.toMap)
    } else {
      new StackOutputSnapshot(Map.empty)
    }
  }

  def loadSnapshot(file: File): Option[StackOutputSnapshot] = {
    if (file.exists()) {
      val pairs =
        for (line <- Source.fromFile(file).getLines();
             parts = line.split(':') if parts.length == 2;
             name = parts(0);
             hash = parts(1)
        ) yield (name, hash)
      Some(new StackOutputSnapshot(pairs.toMap))
    } else {
      None
    }
  }

  private def recursiveListFiles(root: File): Stream[File] =
    FileUtils.listFiles(root, null, true).toStream.asInstanceOf[Stream[File]]

  private def calculateHash(file: File): String =
    (managed(new FileInputStream(file)) map DigestUtils.md5Hex).opt.get
}
