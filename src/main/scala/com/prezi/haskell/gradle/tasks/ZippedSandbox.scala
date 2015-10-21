package com.prezi.haskell.gradle.tasks

import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.tasks.bundling.Zip

/**
 * Zips the projects sandbox to create its main artifact
 */
class ZippedSandbox extends Zip with HaskellProjectSupport with UsesSandbox {

    getDependsOn.addAll(getProject.getTasksByName("compileMain", false))
    getInputs.sourceDir(configTimeSandboxRoot)

    override protected def createCopyAction: CopyAction = {
        from(sandbox.root)
        super.createCopyAction
    }
}
