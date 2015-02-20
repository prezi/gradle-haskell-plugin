# gradle-haskell-plugin
Haskell plugin for Gradle

**This plugin is under active development, not usable yet**

## Usage
```groovy
apply plugin: 'haskell'
```

TODO

## Dependencies, artifacts
Haskell projects creates **zipped sandboxes** as artifacts, and handles dependencies by *chaining* the dependent sandboxes for each GHC/cabal command.

TODO