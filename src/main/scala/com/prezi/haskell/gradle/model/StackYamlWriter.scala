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

  def resolver(name: String): Unit = {
    builder.append(s"resolver: $name\n")
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
      case GHC801WithSierraFix =>
        // Temporary fix for https://github.com/commercialhaskell/stack/issues/2577
        builder.append(
          """
            |compiler-check: match-exact
            |compiler: ghc-8.0.1.20161117
            |setup-info:
            |  ghc:
            |    linux64:
            |      8.0.1.20161117:
            |        url: http://downloads.haskell.org/~ghc/8.0.2-rc1/ghc-8.0.1.20161117-x86_64-deb8-linux.tar.xz
            |        content-length: 112047972
            |        sha1: 6a6e4c9c53c71cc84b6966a9f61948542fd2f15a
            |    macosx:
            |      8.0.1.20161117:
            |        url: https://downloads.haskell.org/~ghc/8.0.2-rc1/ghc-8.0.1.20161117-x86_64-apple-darwin.tar.xz
            |        content-length: 113379688
            |        sha1: 53ed03d986a49ea680c291540ce44ce469514d7c
            |    windows64:
            |      8.0.1.20161117:
            |        url: https://downloads.haskell.org/~ghc/8.0.2-rc1/ghc-8.0.1.20161117-x86_64-unknown-mingw32.tar.xz
            |        content-length: 155652048
            |        sha1: 74118dd8fd8b5e4c69b25df1644273fbe13177c7
          """.stripMargin)
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
