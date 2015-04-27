# gradle-haskell-plugin
[![Build Status](https://travis-ci.org/prezi/gradle-haskell-plugin.svg)](https://travis-ci.org/prezi/gradle-haskell-plugin)

Haskell plugin for Gradle

## Usage

Put your _source code_ to `src/main/haskell`, the _test code_ to `src/test/haskell` and the _cabal file_ to the project's root.
Don't forget to set the source directory in the _cabal file_ too:
```
hs-source-dirs:      src/main/haskell
```

For the default operation all you have to do is _apply the plugin_:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'com.prezi.haskell:gradle-haskell-plugin:0.2+'
    }
}

apply plugin: 'haskell'
```

## Dependencies, artifacts
Haskell projects creates **zipped sandboxes** as artifacts, and handles dependencies by *chaining* the dependent sandboxes for each GHC/cabal command.
All the dependencies put into the configuration called `main` must be haskell dependencies.

The following example shows both an _external dependency_ and a _project dependency_, both pointing to an artifact produced by the this plugin.

```groovy
dependencies {
    main group: "something", name: "lib1", version: "1.+", configuration: 'main'
    main project(path: ":lib2", configuration: "main")
}

```

## Additional options
It is possible to change the _source set_ used to determine the up-to-date status of the compilation task.
See the following example:

```groovy
haskell {
    sources {
        main {
            haskell {
                source.srcDir "TODO"
            }
        }
    }
}
```

**NOTE** This only affects _gradle_'s up-to-date checks. You still have to add the source directories to your _cabal file_ too.

### Profiling
_Profiling_ is enabled by default. To turn it off, change the `profiling` property of the `haskell` extension:

```groovy
haskell {
    profiling = false
}
```

or use the `-Pghc-disable-profiling` command line argument.

### GHC and cabal location

It is possible to specify a _cabal config file_ which can point to a non-default global package database, etc:
```groovy
haskell {
  cabalConfigFile = "~/custom/cabal.cfg"
}
```

or `-Pcabal-config-file=~/custom/cabal.cfg`.

To manage which `cabal`, `ghc` etc. is executed by the plugin, you can override the `PATH` and other environment variables with the following syntax:

```groovy
haskell {
  envConfigurer { Map<String, Object> envMap ->
    def path = envMap.get("PATH")
                
    envMap.put("PATH", [
        "/custom-ghc-path/bin",
        "/custom-cabal-path/bin",
        path
      ].join(":"))
  }
}
```

## Explanation
Let's see an example scenario with _4 gradle-haskell projects_.

![drawing1](https://raw.githubusercontent.com/prezi/gradle-haskell-plugin/master/doc/gradle-haskell-plugin-drawing1.png)

The project called _Haskell project_ depends on two other projects, which taking into accound the transitive dependencies means it depends on _three other haskell projects_. Each project has its own haskell source and _cabal file_. Building this suite consists of the following steps:

- **dependency 1** is built using only the _global package database_, everything **not** in that database, together with the compiled project goes into its `build/sandbox` directory, which is a combination of a _GHC package database_ and the project's build output. This is packed as **dependency 1**'s build artifact.
- For **dependency 2**, Gradle first downloads the build artifact of _dependency 1_ and extracts it to `build/deps/dependency1`. 
- Then it runs [SandFix](https://github.com/exFalso/sandfix) on it
- And compiles the second project, now passing **both** the _global package database_ and **dependency 1**'s sandbox to cabal/ghc. The result is that only the packages which are **not** in any of these two package databases will be installed in the project's own sandbox, which becomes the build artifact of **dependency 2**.
- For **dependency 3**, Gradle extracts both the direct dependency and the transitive dependency's sandbox, to `build/deps/dependency2` and `build/deps/dependency3`.
- Then it runs [SandFix](https://github.com/exFalso/sandfix) on both the dependencies
- And finally passes three package databases to cabal/ghc to compile the project. Only those cabal dependencies will be installed into this sandbox which are not in global, neither in any of the dependent sandboxes.
- Finally, for **Haskell project** it goes the same way, but here we have three sandboxes, all chained together to make sure only the built sandbox only contains what is not in the dependent sandboxes yet.

## Details
Applying the plugin adds the following to the project:

### Fields

- `ghcSandbox` is the project's internal _sandbox model_, used by the plugin
- `ghcSandboxRoot` is the root path of the project's _sandbox_
- `ghcPackageDb` is the _package database_ inside the project's _sandbox_
- `ghcPrefix` is the directory where _cabal_ installs the project in its _sandbox_
- `haskellTools` is a helper object used internally by the plugin

### Configurations

- `main` is used to set up the sandbox chain from the dependencies and it also defines the project's sandbox as its _artifact_
- `test` extends `main` but has no special role currently (because _cabal_ configures the project with its tests in one phase)

### Tasks

- `sandboxInfo` prints some basic information about the project's sandbox
- `sandboxPackages` prints the package list of the project's and its dependent sandboxes
- `compileMain` and `compileTest` all refers to the same task, compiling the whole cabal project
- `assemble` assembles all the files  of the project; by default this means running `compileMain` and generating the _ghc-mod cradle_
- `clean` deletes the `build` and `dist` directories
- `test` compiles the cabal package with tests enabled, and runs them
- `check` executes `test`. It is an extension point to execute other kind of tests.
- `build` executes `assemble` and `check`
- `zipSandbox` packs the whole _sandbox_ as the _artifact_ of the `main` configuration of the project
- `freeze` executes `cabal freeze`
- `repl` executes `cabal repl`

Additional tasks supporting the ones above:
- `sandboxDirectories` creates the sandbox directory structure
- `sandbox` initializes the package database in the project's sandbox
- `copySandFix` extracts the bunled [_SandFix_](https://github.com/exFalso/sandfix) from the plugin
- `configureSandboxes` sets up `extractDependentSandboxes` and `fixDependentSandboxes` tasks after the dependency resolution is done
- `extractDependentSandboxes` extracts the resolved artifacts to temporary directories inside `build/deps`
- `fixDependentSandboxes` clones the extracted sandboxes and runs _SandFix_ on them
- `generateGhcModCradle` generates the `ghc-mod.cradle` file to the project's root directory, supporting [ghc-mod](http://www.mew.org/~kazu/proj/ghc-mod/en/) to find the dependent sandboxes.

### ghc-mod support
The `ghc-mod` support is implemented by the following _pull request_ on the `ghc-mod` side:
https://github.com/kazu-yamamoto/ghc-mod/pull/470
