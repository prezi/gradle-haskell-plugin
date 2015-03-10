package com.prezi.haskell.gradle.extension.impl

import com.prezi.haskell.gradle.ApiHelper._
import com.prezi.haskell.gradle.Names
import com.prezi.haskell.gradle.extension.{HaskellExtension, ProjectExtender}
import com.prezi.haskell.gradle.external.HaskellTools
import com.prezi.haskell.gradle.model.DefaultHaskellSourceSet
import com.prezi.haskell.gradle.tasks.{CompileTask, TestTask}
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.Delete
import org.gradle.api.{DefaultTask, Task}
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.ProjectSourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin.{ASSEMBLE_TASK_NAME, CLEAN_TASK_NAME}

import scala.collection.JavaConverters._

trait HaskellCompilationSupportImpl {
   this : ProjectExtender =>

   protected def instantiator: Instantiator
   protected def fileResolver: FileResolver

   protected def registerExtension(): Unit = {
     createField[HaskellExtension]("haskell", instantiator, project)
   }

   protected def addSourceSets(): Unit = {
     val mainSources = projectSourceSet.maybeCreate("main")
     val testSources = projectSourceSet.maybeCreate("test")

     val mainSourceSet = instantiator.create[DefaultHaskellSourceSet]("haskell", mainSources, fileResolver)
     val cabalSourceSet = instantiator.create[DefaultHaskellSourceSet]("cabal", mainSources, fileResolver)
     val testSourceSet = instantiator.create[DefaultHaskellSourceSet]("haskell", testSources, fileResolver)

     mainSourceSet.getSource.srcDir("src/main/haskell")
     mainSourceSet.getSource.include("**/*.hs")

     testSourceSet.getSource.srcDir("src/test/haskell")
     testSourceSet.getSource.include("**/*.hs")

     cabalSourceSet.getSource.srcDir(".")
     cabalSourceSet.getSource.include("*.cabal")

     mainSources.add(mainSourceSet)
     mainSources.add(cabalSourceSet)
     testSources.add(testSourceSet)
   }

   protected def addCompileTasks(): Unit = {
     for (conf <- project.getConfigurations.asScala) {
       val sourceSet = projectSourceSet.findByName(conf.getName)
       val assembleTask = getTask[Task](ASSEMBLE_TASK_NAME)

       if (sourceSet != null) {
         if (conf.getName == Names.mainConfiguration) {
           val compileConfTask = createTask[CompileTask]("compile" + conf.getName.capitalize)
           compileConfTask.attachToSourceSet(sourceSet)

           compileConfTask.configuration = Some(conf)
           compileConfTask.tools = tools

           assembleTask.dependsOn(compileConfTask)
         } else {
           val compileAliasTask = createTask[DefaultTask]("compile" + conf.getName.capitalize)

           compileAliasTask.dependsOn("compileMain")
           compileAliasTask.dependsOn(conf)

           assembleTask.dependsOn(compileAliasTask)
         }
       }
     }

     if (!isTaskDefined("build")) {
       val buildTask = createTask[DefaultTask]("build")
       buildTask.dependsOn(ASSEMBLE_TASK_NAME)
       buildTask.dependsOn("check")
     }
   }

   protected def addTestTasks(): Unit = {
     val testTask = createTask[TestTask]("test")
     testTask.dependsOn("compileTest")
     testTask.tools = tools
     testTask.configuration = Some(getConfiguration(Names.testConfiguration))

     // The default `check` task just calls the `test` task
     if (!isTaskDefined("check")) {
       val checkTask = createTask[DefaultTask]("check")
       checkTask.getDependsOn.add(testTask)
     }
   }

   protected def extendCleanTask(): Unit = {
     val cleanTask = getTask[Delete](CLEAN_TASK_NAME)
     cleanTask.delete(project.getProjectDir </> "dist")
   }

   private lazy val projectSourceSet: ProjectSourceSet = getField[HaskellExtension]("haskell").getSources
   private lazy val tools: Option[HaskellTools] = Some(getField[HaskellTools]("haskellTools"))
 }
