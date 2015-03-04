package com.prezi.haskell.gradle

import java.io.File

import org.gradle.internal.reflect.Instantiator
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

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
