package com.prezi.haskell.gradle.model

import java.io.File

import org.apache.commons.io.FileUtils

class StackYamlWriter(target: File) {
  val builder = new StringBuilder()

  def flags(pkgFlags: Map[String, Map[String, String]]): Unit = {
    builder.append("flags:\n")

    for ((pkgName, flags) <- pkgFlags) {
      builder.append(s"  $pkgName:\n")
      for ((k, v) <- flags) {
        builder.append(s"    $k: $v\n")
      }
    }
  }

  def packages(pkgs: Seq[String]): Unit = {
    builder.append("packages:")
    stringList(pkgs)
  }

  def extraPackageDbs(dbs: Seq[String]): Unit = {
    builder.append("extra-package-dbs:")
    stringList(dbs)
  }

  def extraDeps(deps: Seq[String]): Unit = {
    builder.append("extra-deps:")
    stringList(deps)
  }

  def localBinPath(path: String): Unit = {
    builder.append(s"local-bin-path: $path\n")
  }

  def ghcVersion(ghcVersion: GHCVersion): Unit = {
    ghcVersion match {
      case GHC802 =>
        builder.append("resolver: ghc-8.0.2\n")
    }
  }

  def close(): Unit = {
    FileUtils.writeStringToFile(target, builder.toString())
  }

  private def stringList(items: Seq[String]): Unit = {
    if (items.isEmpty) {
      builder.append(" []\n")
    } else {
      builder.append('\n')
      for (item <- items) {
        builder.append(s"  - $item\n")
      }
    }
  }
}
