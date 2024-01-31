Kotbase Kermit is a Couchbase Lite custom logger which logs to [Kermit](https://kermit.touchlab.co/). Kermit can direct
its logs to any number of log outputs, including the console.

## Installation

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-ee-kermit:{{ version_full }}")
            }
        }
    }
    ```

=== "Community Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-kermit:{{ version_full }}")
            }
        }
    }
    ```

## Usage

```kotlin
// Disable default console logs and log to Kermit
Database.log.console.level = LogLevel.NONE
Database.log.custom = KermitCouchbaseLiteLogger(kermit)
```
