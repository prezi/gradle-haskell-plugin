package com.prezi.haskell.gradle.systests

import java.io.{BufferedReader, InputStream, InputStreamReader}

class StreamToStdout(stream: InputStream) extends Thread {
  override def run(): Unit = {
    val reader = new BufferedReader(new InputStreamReader(stream))
    try {
      var line: String = reader.readLine()
      while (line != null) {
        println(line)
        line = reader.readLine()
      }
    }
    finally {
      reader.close()
    }
  }
}

object StreamToStdout {
  def apply(stream: InputStream): Unit = {
    new StreamToStdout(stream).run()
  }
}