package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import scala.collection.JavaConverters._

/**
 * Lists the packages in a project's sandbox, and all its dependent sandboxes
 */
class SandboxPackages extends DefaultTask {
   private val sandbox = getProject.getExtensions.getByType(classOf[Sandbox])
   var tools: Option[HaskellTools] = None

   dependsOn("sandbox")

   @TaskAction
   def run(): Unit = {
     if (!tools.isDefined) {
       throw new IllegalStateException("tools is not specified")
     }

     val config = getProject.getConfigurations.getByName("main")
     val depSandboxes =
       config.getResolvedConfiguration.getResolvedArtifacts.asScala.toList
        .map(Sandbox.fromResolvedArtifact(getProject, _))
        .toList

     tools.get.ghcPkgList(depSandboxes.+:(sandbox))
   }
 }
