package com.prezi.haskell.gradle.model

import java.io.File

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact
import org.specs2.mutable._
import org.specs2.mock._

class SandboxSpecs extends SpecificationWithJUnit with Mockito {
  val root = new File("root")
  val sandbox = new Sandbox(root)

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

    "be constructable from a project's resolved artifact" in {
      val project = mock[Project]
      val artifact = mock[ResolvedArtifact]

      val buildDir = root
      val artifactName = "artifact"

      project.getBuildDir returns buildDir
      artifact.getFile returns (new File(artifactName))

      val depSandbox = Sandbox.fromResolvedArtifact(project, artifact)

      depSandbox.root mustEqual buildDir </> "deps" </> artifactName
    }
  }
}
