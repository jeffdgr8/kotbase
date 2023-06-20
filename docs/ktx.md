---
hide:
  - toc
---

The KTX extensions include the excellent [Kotlin extensions by MOLO17](https://github.com/MOLO17/couchbase-lite-kotlin),
as well as other convenience functions for composing queries, observing change `Flow`s, and creating indexes.

## Installation

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation("dev.kotbase:couchbase-lite-ee-ktx:{{ version }}")
                }
            }
        }
    }
    ```

=== "Community Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation("dev.kotbase:couchbase-lite-ktx:{{ version }}")
                }
            }
        }
    }
    ```
