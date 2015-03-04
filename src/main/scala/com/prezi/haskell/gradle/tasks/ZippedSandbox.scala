package com.prezi.haskell.gradle.tasks

import org.gradle.api.tasks.bundling.Zip

/**
 * Zips the projects sandbox to create its main artifact
 */
class ZippedSandbox extends Zip with HaskellProjectSupport {

    getDependsOn.addAll(getProject.getTasksByName("compileMain", false))
    from(sandbox.root)
}
