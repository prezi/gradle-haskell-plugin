package com.prezi.haskell.gradle.model

import com.prezi.haskell.gradle.incubating.{FunctionalSourceSet, LanguageSourceSet, AbstractLanguageSourceSet}
import org.gradle.api.internal.file.{DefaultSourceDirectorySet, FileResolver}

/**
 * Source set for Haskell projects
 */
trait HaskellSourceSet extends LanguageSourceSet {

}

class DefaultHaskellSourceSet(name: String, parentName: FunctionalSourceSet, fileResolver: FileResolver)
  extends AbstractLanguageSourceSet(name, parentName, "Haskell source", new DefaultSourceDirectorySet("source", fileResolver))
  with HaskellSourceSet {

}