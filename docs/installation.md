Add the Kotbase dependency to your [Kotlin Multiplatform project](
https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html) in the **commonMain** source set dependencies of
your shared module's **build.gradle.kts**:

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation("dev.kotbase:couchbase-lite-ee:{{ version }}")
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
                    implementation("dev.kotbase:couchbase-lite:{{ version }}")
                }
            }
        }
    }
    ```

Kotbase is published to Maven Central. The Couchbase Lite Enterprise Edition dependency additionally requires the
Couchbase Maven repository.

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    repositories {
        mavenCentral()
        maven("https://mobile.maven.couchbase.com/maven2/dev/")
    }
    ```

=== "Community Edition"

    ```kotlin title="build.gradle.kts"
    repositories {
        mavenCentral()
    }
    ```
