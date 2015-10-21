package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.ApiHelper
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip

/**
 * Zips the projects sandbox to create its main artifact
 */
class ZippedSandbox extends Zip with HaskellProjectSupport with UsesSandbox {

    getDependsOn.addAll(getProject.getTasksByName("compileMain", false))
    getProject.getTasks.findByName("stackPath").doLast(ApiHelper.asClosure[Task] { _ =>
        from(sandbox.root)
        getLogger.debug("stackPath ran, set source to {}", sandbox.root.getAbsolutePath)
    })
}
