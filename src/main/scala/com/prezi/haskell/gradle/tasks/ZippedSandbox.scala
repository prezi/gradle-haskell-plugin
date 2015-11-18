package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper._
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.bundling.Zip

/**
 * Zips the projects sandbox to create its main artifact
 */
class ZippedSandbox
  extends Zip
  with HaskellProjectSupport
  with UsesSandbox
  with TaskLogging {

    getDependsOn.addAll(getProject.getTasksByName("compileMain", false))
    getInputs.sourceDir(configTimeSandboxRoot)

    override protected def createCopyAction: CopyAction = {
      if (haskellExtension.isExecutable) {
        val dir = getProject.getBuildDir </> "sandbox"
        debug(s"Zipping $dir")
        from(dir)
      } else {
        debug(s"Zipping ${sandbox.root}")
        from(sandbox.root)
      }
      super.createCopyAction
    }
}
