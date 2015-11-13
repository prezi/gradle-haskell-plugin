package com.prezi.haskell.gradle.model.sandboxstore

import com.prezi.haskell.gradle.model.{Sandbox, SandboxArtifact}
import org.gradle.api.GradleException

trait SandboxStore {

  /**
    * Fixes and stores a sandbox artifact
    * @param depSandbox The sandbox artifact to be stored
    * @param dependencies Its dependencies, required to be able to fix the sandbox
    * @return Returns the outcome of the store opration (cached or it already existed)
    */
  def store(depSandbox: SandboxArtifact, dependencies: Set[SandboxArtifact]): SandBoxStoreResult

  /**
    * Gets the location for a given sandbox artifact where it would be stored.
    *
    * It does not require the sandbox to be actually in the store.
    * @param depSandbox The sandbox artifact to look for
    * @return Returns a sandbox descriptor with paths pointing to the store's appropriate subdirectory
    */
  def find(depSandbox: SandboxArtifact): Sandbox

  /**
    * Gets the location for a given sandbox artifact where it would be stored.
    *
    * @throws GradleException if the given sandbox is not in the store
    * @param sandbox The sandbox artifact to look for
    * @return Returns a sandbox descriptor with paths pointing to the store's appropriate subdirectory
    */
  def get(sandbox: SandboxArtifact): Sandbox
}


