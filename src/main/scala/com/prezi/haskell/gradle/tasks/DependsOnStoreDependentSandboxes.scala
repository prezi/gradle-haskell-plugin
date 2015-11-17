package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper
import org.gradle.api.{GradleException, Task}
import scala.util.{Failure, Success, Try}
import java.lang.Boolean

/**
  * Defines dependency on the 'storeDependentSandboxes' task with a custom up-to-date check
  * based on the result of that task.
  */
trait DependsOnStoreDependentSandboxes {
  this: Task =>

  dependsOn("storeDependentSandboxes")

  getOutputs.upToDateWhen (ApiHelper.asClosureWithReturn { _: AnyRef =>
    (for {
      storeDependentSandboxesTask <- Try { getProject.getTasksByName("storeDependentSandboxes", false).iterator().next() }
      isAnySandboxUpdated <- Try { storeDependentSandboxesTask.asInstanceOf[StoreDependentSandboxes].isAnySandboxUpdated }
    } yield isAnySandboxUpdated) match {
      case Success(bool) => new Boolean(!bool)
      case Failure(e) => throw new GradleException("Failed to get storeDependentSandboxes.isAnySandboxUpdated!", e)
    }
  })

}
