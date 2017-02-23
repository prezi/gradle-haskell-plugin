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
        classpath 'com.prezi.haskell:gradle-haskell-plugin:0.4+'
    }
}

apply plugin: 'haskell'
```

## Dependencies, artifacts
Haskell projects creates **zipped sandboxes** as artifacts, and handles dependencies by *chaining* the dependent sandboxes for each GHC/cabal/stack command.
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

## Stack support
The plugin now only works through [stack](http://haskellstack.com). 

In this case all you need is a working `stack` executable, everything else is handled by the plugin and *stack*.

There are a few additional options available in *stack mode*:

### Compiler and snapshot versions
To change the GHC version or the *stackage snapshot* to be used, use:

```groovy
haskell {
    ghcVersion = "ghc-7.10.2"
    snapshotId = "lts-3.19"
}
```

### Package flags
It is possible to customize the *cabal flags* of dependencies installed by *stack*, with the following syntax:
 
 ```groovy
 haskell {
 	packageFlags["text"] = ["integer-simple": "false"]
 }
 ```

### Profiling
_Profiling_ is disabled by default. To turn it on, change the `profiling` property of the `haskell` extension:

```groovy
haskell {
    profiling = true
}
```

or use the `-Pghc-enable-profiling` command line argument.

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

## Explanation (stack mode)
The stack mode uses the `extra-package-dbs` option of *stack* which was introduced to support this plugin. The idea is that gradle generates the `stack.yaml` 
based on the existing `.cabal` file and the gradle project, and this way it can set up the stack project to use the dependent gradle projects as
binary artifacts.

The generated *stack* projects have the following properties:
- They use a compiler-only resolver (for example `ghc-7.10.2`)
- All other dependencies are listed in the `extra-deps` section
- The gradle-level dependencies are listed in as `extra-package-dbs`

This way the *stack* project's *local package database* can be archived as a binary artifact.
 
To not loose the stackage *snapshots*, the plugin also uses another tool called [snapshot-versions](https://github.com/vigoo/snapshot-versions), which
generates the `extra-deps` section of the *stack project* by enumerating all the dependencies from the `.cabal` file and reading the snapshot version number
from the configured stackage snapshot.

To use a package that is **not** part of the configured *stackage snapshot*, but otherwise available from *hackage*, you have to specify it's exact version number 
in the `.cabal file`, like:

```
Crypto ==4.2.5.1
```

For the dependencies that are *part* of the snapshot, the `.cabal` file should not put any constraints on.

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
