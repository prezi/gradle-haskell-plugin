package com.prezi.haskell.gradle.model

import org.gradle.api.internal.file.{DefaultSourceDirectorySet, FileResolver}
import org.gradle.language.base.internal.AbstractLanguageSourceSet
import org.gradle.language.base.{FunctionalSourceSet, LanguageSourceSet}

/**
 * Source set for Haskell projects
 */
trait HaskellSourceSet extends LanguageSourceSet {

}

class DefaultHaskellSourceSet(name: String, parentName: FunctionalSourceSet, fileResolver: FileResolver)
  extends AbstractLanguageSourceSet(name, parentName, "Haskell source", new DefaultSourceDirectorySet("source", fileResolver))
  with HaskellSourceSet {

}