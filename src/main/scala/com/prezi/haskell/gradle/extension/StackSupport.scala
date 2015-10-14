package com.prezi.haskell.gradle.extension

import com.prezi.haskell.gradle.extension.impl.StackSupportImpl
import org.gradle.api.Project

class StackSupport(protected val project: Project)
  extends StackSupportImpl
  with ProjectExtender {

  addTasks
  extendCleanTask
}
