package com.prezi.haskell.gradle.systests

import com.prezi.haskell.gradle.ApiHelper._
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuildingTestProject1Specs extends BuildingTestProjectSpecsBase {
  val testName = "test1"
  val lib2ReplacePattern = "text greeting"
}

@RunWith(classOf[JUnitRunner])
class BuildingTestProject2Specs extends BuildingTestProjectSpecsBase {
  val testName = "test2"
  val lib2ReplacePattern = "text \\(hello greeting\\)"
}

trait BuildingTestProjectSpecsBase
  extends SpecificationWithJUnit
  with UsingTestProjects {

  def testName: String
  def lib2ReplacePattern: String

  sequential

  "test environment" should {
    "copy the test projects to a temporary location" in {
      withCleanWorkingDir(testName) { root =>
        buildGradleExists(root) aka "build.gradle exists" must beTrue
      }
    }
  }

  "build in stack mode" should {
    "be able to build 'app' from clean state" in {
      withCleanWorkingDir(testName) { root =>
        gradle(root, "app:build") aka "gradle app:build runs successfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }

    "recompile successfully if 'lib1' is changed" in {
      withCleanWorkingDir(testName) { root =>
        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "recompile successfully if 'lib1' is changed and recompiled" in {
      withCleanWorkingDir(testName) { root =>
        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "lib1:build") aka "gradle lib1:build runs successfully" must beTrue
        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "recompile successfully if 'lib2' is changed" in {
      withCleanWorkingDir(testName) { root =>
        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib2" </> "src" </> "main" </> "haskell" </> "Lib2.hs", lib2ReplacePattern, "text \"hi\"")

        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hi"
      }
    }

    "recompile successfully if 'lib2' is changed and recompiled" in {
      withCleanWorkingDir(testName) { root =>
        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib2" </> "src" </> "main" </> "haskell" </> "Lib2.hs", lib2ReplacePattern, "text \"hi\"")

        gradle(root, "lib2:build") aka "gradle lib2:build runs successfully" must beTrue
        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hi"
      }
    }

    "be able to build 'app' from clean state with 'text' as dependency" in {
      withCleanWorkingDir("test1-with-text") { root =>
        gradle(root, "app:build") aka "gradle app:build runs successfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }
  }
}
