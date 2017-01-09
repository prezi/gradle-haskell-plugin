package com.prezi.haskell.gradle.systests

import com.prezi.haskell.gradle.ApiHelper._
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BuildingInPrideSpecs
  extends SpecificationWithJUnit
  with UsingTestProjects
  with UsingPride {

  sequential

  "build in stack mode" should {
    "be able to build 'app' from clean state" in {
      withCleanWorkingDir("test1-pride") { root =>
        initWorkspace(root)

        gradle(root, "app:build") aka "gradle app:build runs successfully" must beTrue
        appOutputExists(root) aka "build result exists" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"
      }
    }

    "recompile succesfully if 'lib1' is changed" in {
      withCleanWorkingDir("test1-pride") { root =>
        initWorkspace(root)

        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "recompile succesfully if 'lib1' is changed and recompiled" in {
      withCleanWorkingDir("test1-pride") { root =>
        initWorkspace(root)

        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib1" </> "src" </> "main" </> "haskell" </> "Lib1.hs", "\"hello \"", "\"hey \"")

        gradle(root, "lib1:build") aka "gradle lib1:build runs successfully" must beTrue
        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hey world"
      }
    }

    "recompile successfully if 'lib2' is changed" in {
      withCleanWorkingDir("test1-pride") { root =>
        initWorkspace(root)

        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib2" </> "src" </> "main" </> "haskell" </> "Lib2.hs", "text greeting", "text \"hi\"")

        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hi"
      }
    }

    "recompile successfully if 'lib2' is changed and recompiled" in {
      withCleanWorkingDir("test1-pride") { root =>
        initWorkspace(root)

        gradle(root, "app:build") aka "First gradle app:build runs successfully" must beTrue
        runApp(root) aka "the compiled app's output" must be equalTo "hello world"

        modifySource(root </> "lib2" </> "src" </> "main" </> "haskell" </> "Lib2.hs", "text greeting", "text \"hi\"")

        gradle(root, "lib2:build") aka "gradle lib2:build runs successfully" must beTrue
        gradle(root, "app:build") aka "Second gradle app:build runs successfully" must beTrue
        runApp(root) aka "the recompiled app's output" must be equalTo "hi"
      }
    }

  }
}
