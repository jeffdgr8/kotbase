# Module couchbase-lite-ee

## Couchbase Lite Enterprise Edition

Kotbase core Couchbase Lite Enterprise Edition library

### Installation

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("dev.kotbase:couchbase-lite-ee:3.0.12-1.0.0")
            }
        }
    }
}
```

```kotlin
repositories {
    mavenCentral()
    maven("https://mobile.maven.couchbase.com/maven2/dev/")
}
```

### Usage

See usage guide at [kotbase.dev](https://kotbase.dev/).