package com.prezi.haskell.gradle.model

sealed trait GHCVersion
case object GHC7102 extends GHCVersion
case object GHC801WithSierraFix extends GHCVersion
case object GHC801 extends GHCVersion
case object GHC802 extends GHCVersion
