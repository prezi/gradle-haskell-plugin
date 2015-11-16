package com.prezi.haskell.gradle.unittests

import java.io.File

import com.prezi.haskell.gradle.ApiHelper
import org.gradle.internal.reflect.Instantiator
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ApiHelperSpecs extends SpecificationWithJUnit with Mockito {

  "asAction" should {
    "create an equivalent action" in {
      val input: String = "a"
      var target: String = ""
      val fn = (input: String) => target = input + input

      val action = ApiHelper.asAction[String](fn)
      action.execute(input)

      target mustEqual "aa"
    }
  }

  "asClosure" should {
    "create an equivalent closure" in {
      val input: String = "b"
      var target: String = ""
      val fn = (input: String) => target = input + input

      val closure = ApiHelper.asClosure[String](fn)
      closure.call(input)

      target mustEqual "bb"
    }
  }

  "asClosureWithReturn" should {
    "create an equivalent closure" in {
      val input: String = "b"
      val fn = (input: String) => input + input

      val closure = ApiHelper.asClosureWithReturn[String, String](fn)

      closure.call(input) mustEqual "bb"
    }
  }

  "instantiatorExt" should {
    "call instantiator's newInstance method" in {
      val instantiator = mock[Instantiator]

      case class TestClass()
      import ApiHelper._

      instantiator.create[TestClass]("a", 11 : java.lang.Integer)

      there was one(instantiator).newInstance(classOf[TestClass], "a", 11 : java.lang.Integer)
    }
  }

  "fileExt" should {
    import ApiHelper._

    "be able to combine paths" in {
      new File("root") </> "child" mustEqual new File("root/child")
    }

    "combining with empty string is id" in {
      new File("root") </> "" mustEqual new File("root")
    }
  }
}
