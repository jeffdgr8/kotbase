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

## Vector Search

!!! important "This is an [Enterprise Edition](https://www.couchbase.com/products/editions/mobile/) feature."

Enterprise users can also download the Couchbase Lite [Vector Search](vector-search.md) extension library.

!!! note

    To use Vector Search, you must have Couchbase Lite installed and add the Vector Search extension to your Couchbase
    Lite application. Vector Search is available only for 64-bit architectures and Intel processors that support the
    Advanced Vector Extensions 2 (AVX2) instruction set. To verify whether your device supports the AVX2 instructions
    set, [follow these instructions](
    https://www.intel.com/content/www/us/en/support/articles/000090473/processors/intel-core-processors.html).

### Install Extension Libraries

Install the [Vector Search](vector-search.md) library for each of the platforms your KMP project targets.

#### Android

```kotlin title="build.gradle.kts"
kotlin {
    sourceSets {
        ...
        androidMain.dependencies {
            // All standard 64-bit ARM architectures
            implementation("com.couchbase.lite:couchbase-lite-android-vector-search-arm64:{{ version_vector_search }}")
            // For x86_64 architectures
            implementation("com.couchbase.lite:couchbase-lite-android-vector-search-x86_64:{{ version_vector_search }}")
        }
    }
}
```

#### Java

```kotlin title="build.gradle.kts"
kotlin {
    sourceSets {
        ...
        jvmMain.dependencies {
            implementation("com.couchbase.lite:couchbase-lite-java-vector-search:{{ version_vector_search }}")
        }
    }
}
```

#### iOS + macOS

=== "Direct Download"

    1. Download the binaries from here — [binary download link](
       https://packages.couchbase.com/releases/couchbase-lite-vector-search/{{ version_vector_search }}/couchbase-lite-vector-search_xcframework_{{ version_vector_search }}.zip).
    2. Unpack the download zip file into your Xcode project location.
    3. Select your target settings in Xcode and drag **CouchbaseLiteVectorSearch.xcframework** from your Finder to the
       **Frameworks, Libraries, and Embedded Content** section.
    4. Start using Couchbase Lite Vector Search with Kotbase in your project.

=== "CocoaPods"

    The [Kotlin CocoaPods Gradle plugin](
    https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-cocoapods-overview.html) can be used to
    generate a [Podspec](https://guides.cocoapods.org/syntax/podspec.html) for your project that includes the
    `CouchbaseLiteVectorSearch` dependency. Use `linkOnly = true` to link the dependency without generating Kotlin
    Objective-C interop:

    ```kotlin title="build.gradle.kts"
    plugins {
        kotlin("multiplatform")
        kotlin("native.cocoapods")
    }
    
    kotlin {
        cocoapods {
            ...
            pod("CouchbaseLiteVectorSearch", version = "{{ version_vector_search }}", linkOnly = true)
        }
    }
    ```

=== "Swift Package Manager"

    !!! note
    
        Using Swift Package Manager to install `CouchbaseLiteVectorSearch` requires Xcode 12+.
    
    You can add `CouchbaseLiteVectorSearch` to your app using Swift Package Manger (SPM).
    
    1. Open the project to which you are going to `add CouchbaseLiteVectorSearch`
    2. Open the Project Editor to add a dependency.
        1. In _Project Navigator_:
           **Select** your XCode project file (for example, `HostApp` in the example)
           Xcode opens the _Project Editor_ pane
        2. In the _Project Editor_ pane:
           **Select Project > Swift Packages** and **[+]** to add the dependency
           Xcode opens the _Choose Package Repository_ dialog
        3. In the _Choose Package Repository_ dialog:
           **Enter** the appropriate Couchbase Lite URL, **[Next]** to continue
           For Vector Search: https://github.com/couchbase/couchbase-lite-vector-search-spm.git
        4. **Enter** the required _Version_ ({{ version_vector_search }}) and **[Next]** to continue
        5. **[Finish]** to close the _Choose Package Repository_ dialog

    Xcode displays the name, version and URL of the added `CouchbaseLiteVectorSearch` package.


#### Linux + Mingw

Before you can use Vector Search, you must [download and install the Vector Search library](
https://docs.couchbase.com/couchbase-lite/3.2/c/gs-install.html#vs-release-1-0-0) to the location in your project where
the library can be accessed and loaded at run time. The Vector Search extension for the C platform ships with supported
prebuilt libraries containing the required dependencies.

You need to set the `CBLITE_VECTOR_SEARCH_LIB_PATH` environment variable to the extension location instead of installing
the libraries yourself. If this environment variable is not set, then Kotbase will attempt to find the library in the
current directory.

### Enable Extension

Enable the vector search extension using the following snippet:

```kotlin
Extension.enableVectorSearch()
```

!!! important

    You must enable the extension before you open your database.
