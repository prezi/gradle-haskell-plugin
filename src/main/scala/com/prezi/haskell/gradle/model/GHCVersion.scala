package com.prezi.haskell.gradle.model

sealed trait GHCVersion
case object GHC801WithSierraFix extends GHCVersion
