package com.prezi.haskell.gradle.systests

import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuildingTestProjectSpecs
  extends SpecificationWithJUnit
  with UsingTestProjects {

  sequential

  "build in cabal mode" should {
    "be able to build 'app' from clean state" in {
      withCleanWorkingDir { root =>
        buildGradleExists(root) aka "build.gradle exists" must beTrue
        gradle(root, "app:build") aka "gradle app:build runs succesfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }
  }

  "build in stack mode" should {
    "be able to build 'app' from clean state" in {
      withCleanWorkingDir { root =>
        buildGradleExists(root) aka "build.gradle exists" must beTrue
        gradle(root, "-Puse-stack", "app:build") aka "gradle app:build runs succesfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }
  }
}
