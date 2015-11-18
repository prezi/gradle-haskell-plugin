package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.bundling.Zip

/**
 * Zips the projects built executables to create its main artifact
 */
class ZippedExecutables
  extends Zip
  with HaskellProjectSupport
  with UsesSandbox
  with TaskLogging {

    private val dir = getProject.getBuildDir </> "sandbox"

    getDependsOn.addAll(getProject.getTasksByName("compileMain", false))
    getInputs.sourceDir(dir)

    override protected def createCopyAction: CopyAction = {
        debug(s"Zipping $dir")
        from(dir)
        super.createCopyAction
    }
}
