---
hide:
  - toc
---

The paging extensions are built on Cash App's [Multiplatform Paging](https://github.com/cashapp/multiplatform-paging),
Google's [AndroidX Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) with support
for both Android and iOS. Kotbase Paging provides a [`PagingSource`](
https://developer.android.com/reference/kotlin/androidx/paging/PagingSource) which performs limit/offset paging queries
based on a user-supplied database query.

## Installation

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation("dev.kotbase:couchbase-lite-ee-paging:{{ version }}")
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
                    implementation("dev.kotbase:couchbase-lite-paging:{{ version }}")
                }
            }
        }
    }
    ```
