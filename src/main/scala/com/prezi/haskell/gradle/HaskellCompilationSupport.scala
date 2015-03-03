package com.prezi.haskell.gradle

import com.prezi.haskell.gradle.extension.HaskellExtension
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.DefaultHaskellSourceSet
import com.prezi.haskell.gradle.tasks.{TestTask, BuildTask}
import org.gradle.api.{DefaultTask, Task, Project}
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.ProjectSourceSet

import scala.collection._
import scala.collection.JavaConverters._

/**
 * Adds source sets and build tasks to a project
 * @param project The project the plugin is applied to
 * @param instantiator Gradle object instantiator
 * @param fileResolver File resolver, needed for the source sets
 */
class HaskellCompilationSupport(
  protected val project: Project,
  protected val instantiator: Instantiator,
  protected val fileResolver: FileResolver)
  extends HaskellCompilationSupportImpl with ProjectExtender {

  registerExtension
  addSourceSets
  addBuildTasks
  addTestTasks
}

trait HaskellCompilationSupportImpl {
  this : ProjectExtender =>

  protected def instantiator: Instantiator
  protected def fileResolver: FileResolver

  protected def registerExtension(): Unit = {
    project.getExtensions.create[HaskellExtension]("haskell", classOf[HaskellExtension], instantiator)
  }

  protected def addSourceSets(): Unit = {
    val mainSources = projectSourceSet.maybeCreate("main")
    val testSources = projectSourceSet.maybeCreate("test")

    val mainSourceSet = instantiator.newInstance(classOf[DefaultHaskellSourceSet], "haskell", mainSources, fileResolver)
    val cabalSourceSet = instantiator.newInstance(classOf[DefaultHaskellSourceSet], "cabal", mainSources, fileResolver)
    val testSourceSet = instantiator.newInstance(classOf[DefaultHaskellSourceSet], "haskell", testSources, fileResolver)

    mainSourceSet.getSource.srcDir("src/main/haskell")
    mainSourceSet.getSource.include("*.hs")

    testSourceSet.getSource.srcDir("src/test/haskell")
    testSourceSet.getSource.include("*.hs")

    cabalSourceSet.getSource.srcDir(".")
    cabalSourceSet.getSource.include("*.cabal")

    mainSources.add(mainSourceSet)
    mainSources.add(cabalSourceSet)
    testSources.add(testSourceSet)
  }

  protected def addBuildTasks(): Unit = {
    val buildTasks = mutable.Set[Task]()

    for (conf <- project.getConfigurations.asScala) {
      val sourceSet = projectSourceSet.findByName(conf.getName)

      if (sourceSet != null) {
        val buildConfTask = project.getTasks.create("build" + conf.getName.capitalize, classOf[BuildTask])
        buildConfTask.getDependsOn.add(sourceSet)

        buildConfTask.configuration = Some(conf)
        buildConfTask.tools = tools

        buildTasks.add(buildConfTask)
      }
    }

    if (project.getTasks.findByName("build") == null) {
      val buildTask = project.getTasks.create("build", classOf[DefaultTask])
      buildTask.getDependsOn.addAll(buildTasks.asJavaCollection)
    }
  }

  protected def addTestTasks(): Unit = {
    val testTask = project.getTasks.create("test", classOf[TestTask])
    testTask.dependsOn("buildTest")
    testTask.tools = tools
    testTask.configuration = Some(project.getConfigurations.getByName(Names.testConfiguration))

    // The default `check` task just calls the `test` task
    if (project.getTasks.findByName("check") == null) {
      val checkTask = project.getTasks.create("check", classOf[DefaultTask])
      checkTask.getDependsOn.add(testTask)
    }
  }

  private lazy val projectSourceSet: ProjectSourceSet =
    project.getExtensions.getByType(classOf[HaskellExtension]).getSources

  private lazy val tools: Option[HaskellTools] = Some(getField[HaskellTools]("haskellTools"))
}