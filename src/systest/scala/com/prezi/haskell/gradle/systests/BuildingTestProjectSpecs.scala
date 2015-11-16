package com.prezi.haskell.gradle.systests

import com.prezi.haskell.gradle.ApiHelper._
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuildingTestProjectSpecs
  extends SpecificationWithJUnit
  with UsingTestProjects {

  sequential

  "test environment" should {
    "copy the test projects to a temporary location" in {
      withCleanWorkingDir("test1") { root =>
        buildGradleExists(root) aka "build.gradle exists" must beTrue
      }
    }
  }

  "build in cabal mode" should {
    "be able to build 'app' from clean state" in {
      withCleanWorkingDir("test1") { root =>
        gradle(root, "app:build") aka "gradle app:build runs succesfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }

    "recompile succesfully if 'lib1' is changed" in {
      withCleanWorkingDir("test1") { root =>
        gradle(root, "app:build") aka "First gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "app:build") aka "Second gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "recompile succesfully if 'lib1' is changed and recompiled" in {
      withCleanWorkingDir("test1") { root =>
        gradle(root, "app:build") aka "First gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "lib1:build") aka "gradle lib1:build runs succesfully" must beTrue
        gradle(root, "app:build") aka "Second gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }
  }

  "build in stack mode" should {
    "be able to build 'app' from clean state" in {
      withCleanWorkingDir("test1") { root =>
        gradle(root, "-Puse-stack", "app:build") aka "gradle app:build runs succesfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }

    "recompile succesfully if 'lib1' is changed" in {
      withCleanWorkingDir("test1") { root =>
        gradle(root, "-Puse-stack", "app:build") aka "First gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "-Puse-stack", "app:build") aka "Second gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "recompile succesfully if 'lib1' is changed and recompiled" in {
      withCleanWorkingDir("test1") { root =>
        gradle(root, "-Puse-stack", "app:build") aka "First gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "-Puse-stack", "lib1:build") aka "gradle lib1:build runs succesfully" must beTrue
        gradle(root, "-Puse-stack", "app:build") aka "Second gradle app:build runs succesfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "be able to build 'app' from clean state with 'text' as dependency" in {
      withCleanWorkingDir("test1-with-text") { root =>
        gradle(root, "-Puse-stack", "app:build") aka "gradle app:build runs succesfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }
  }
}
