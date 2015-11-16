package com.prezi.haskell.gradle.unittests.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.model.CabalSandbox
import org.junit.runner.RunWith
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SandboxSpecs extends SpecificationWithJUnit with Mockito {
  val root = new File("root")
  val sandbox = new CabalSandbox(root)

  "Sandbox" should {
    "define the package database as a subdirectory" in {
      sandbox.packageDb mustEqual(root </> "packages")
    }

    "define the ghc installation prefix as a subdirectory" in {
      sandbox.installPrefix mustEqual(root </> "files")
    }

    "be able to generate package-db argument" in {
      sandbox.asPackageDbArg mustEqual "--package-db=root/packages"
    }

    "be able to generate prefix argument" in {
      sandbox.asPrefixArg mustEqual "--prefix=root/files"
    }
  }
}
