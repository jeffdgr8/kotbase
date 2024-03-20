Add the Kotbase dependency to your [Kotlin Multiplatform project](
https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html) in the **commonMain** source set dependencies of
your shared module's **build.gradle.kts**:

=== "Enterprise Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite-ee:{{ version_full }}")
            }
        }
    }
    ```

=== "Community Edition"

    ```kotlin title="build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("dev.kotbase:couchbase-lite:{{ version_full }}")
            }
        }
    }
    ```

!!! note
    The Couchbase Lite Community Edition is free and open source. The Enterprise Edition is free for development and
    testing, but requires a [license from Couchbase](https://www.couchbase.com/pricing/#couchbase-mobile) for production
    use. [See Community vs Enterprise Edition.](https://www.couchbase.com/products/editions/mobile/)

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

## Native Platforms

Native platform targets should additionally link to the Couchbase Lite dependency native binary. See [Supported
Platforms](platforms.md) for more details.

## Linux

Targeting JVM running on Linux or native Linux, both require a specific version of the libicu dependency. (You will see
an error such as `libLiteCore.so: libicuuc.so.71: cannot open shared object file: No such file or directory` indicating
the expected version.) If the required version isn't available from your distribution's package manager, you can
download it from [GitHub](https://github.com/unicode-org/icu/releases).
