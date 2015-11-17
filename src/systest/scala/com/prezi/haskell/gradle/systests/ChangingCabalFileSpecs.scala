package com.prezi.haskell.gradle.systests

import com.prezi.haskell.gradle.ApiHelper._
import org.junit.runner.RunWith
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ChangingCabalFileSpecs
  extends SpecificationWithJUnit
  with UsingTestProjects {

  sequential

  "changing the .cabal file" should {
    "trigger regeneration of stack.yaml" in {
      withCleanWorkingDir("basic") { root =>
        gradle(root, "-Puse-stack", "generateStackYaml")
        stackYamlExists(root) aka "stack.yaml exists" must beTrue
        stackYamlLines(root) aka "initial stack.yaml has no extra-deps" must contain("extra-deps: []")

        modifySource(root </> "test.cabal", "base", "base, ansi-wl-pprint")

        gradle(root, "-Puse-stack", "generateStackYaml")
        stackYamlExists(root) aka "stack.yaml exists" must beTrue

        val lines = stackYamlLines(root)
        lines aka "rebuilt stack.yaml refers to ansi-terminal" must containMatch("ansi-terminal")
        lines aka "rebuilt stack.yaml refers to ansi-wl-pprint" must containMatch("ansi-wl-pprint")
      }
    }
  }

}
