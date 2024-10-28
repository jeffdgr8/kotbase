# Module couchbase-lite-ee-kermit

## Couchbase Lite Enterprise Edition – Kermit Logging Extensions

Kotbase Kermit is a Couchbase Lite custom logger which logs to [Kermit](https://kermit.touchlab.co/). Kermit can direct
its logs to any number of log outputs, including the console.

### Installation

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("dev.kotbase:couchbase-lite-ee-kermit:3.1.9-1.1.1")
        }
    }
}
```

### Usage

```kotlin
// Disable default console logs and log to Kermit
Database.log.console.level = LogLevel.NONE
Database.log.custom = KermitCouchbaseLiteLogger(kermit)
```
