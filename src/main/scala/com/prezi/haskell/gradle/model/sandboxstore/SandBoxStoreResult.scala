package com.prezi.haskell.gradle.model.sandboxstore

sealed trait SandBoxStoreResult {
  def toNormalizedString:String
}

object SandBoxStoreResult {

  def apply(str:String): SandBoxStoreResult = {
    str match {
      case "Created" => Created
      case "AlreadyExists" => AlreadyExists
      case s => throw sys.error(s"Illegal SandBoxStoreResult: $s")
    }
  }

  case object Created extends SandBoxStoreResult {
    override def toNormalizedString: String = "Created"
  }
  case object AlreadyExists extends SandBoxStoreResult {
    override def toNormalizedString: String = "AlreadyExists"
  }
}