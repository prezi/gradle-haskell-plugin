package com.prezi.haskell.gradle.tasks

import com.prezi.haskell.gradle.model.Sandbox
import org.gradle.api.tasks.bundling.Zip

class ZippedSandbox extends Zip {

    getDependsOn.addAll(getProject.getTasksByName("build", false))
    from(getProject.getExtensions.getByType(classOf[Sandbox]).root)
}
