package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.impl.ZippedSandboxArtifactSupportImpl
import org.gradle.api.Project

/**
 * Adds the project's sandbox as an artifact of the project
 */
class ZippedSandboxArtifactSupport(protected val project: Project) extends ZippedSandboxArtifactSupportImpl with ProjectExtender {

    defineZipSandboxArtifact
}


