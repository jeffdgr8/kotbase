Kotbase provides a common [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) API for [Couchbase
Lite](https://www.couchbase.com/products/lite/), allowing you to develop a single Kotlin shared library, which compiles
to native binaries that can be consumed by native apps on each of the supported platforms: Android, JVM, iOS, macOS,
Linux, and Windows.

## Android :fontawesome-brands-android: + JVM :fontawesome-brands-java:

Kotbase implements support for JVM desktop and Android apps via the [Couchbase Lite Java and Android SDKs](
https://github.com/couchbase/couchbase-lite-java-ce-root). Kotbase's API mirrors the Java SDK as much as feasible, which
allows for smooth migration for existing Kotlin code currently utilizing either the Java or Android KTX SDKs.
See [Differences from Couchbase Lite Java SDK](differences.md) for details about where the APIs differ.

Kotbase will pull in the correct Couchbase Lite Java dependencies via Gradle.

### Minification

An application that enables ProGuard minification must ensure that certain pieces of Couchbase Lite library code are not changed.

??? example "Near-minimal rule set that retains the needed code"

    ```title="proguard-rules.pro"
    -dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
    
    -keep class com.couchbase.lite.ConnectionStatus { <init>(...); }
    -keep class com.couchbase.lite.LiteCoreException { static <methods>; }
    -keep class com.couchbase.lite.internal.replicator.CBLTrustManager {
        public java.util.List checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String, java.lang.String);
    }
    -keep interface com.couchbase.lite.internal.ReplicationCollection$C4Filter
    -keep class com.couchbase.lite.internal.ReplicationCollection {
        static <methods>;
        <fields>;
    }
    -keep class com.couchbase.lite.internal.fleece.FLSliceResult { static <methods>; }
    -keep class com.couchbase.lite.internal.core.C4* {
        static <methods>;
        <fields>;
        <init>(...);
    }
    ```

### Android

| API |       x86        |       x64        |      ARM32       |      ARM64       |
|:---:|:----------------:|:----------------:|:----------------:|:----------------:|
| 22+ | :material-check: | :material-check: | :material-check: | :material-check: |

### JVM

| JDK |    Linux x64     |    macOS x64     |   Windows x64    |
|:---:|:----------------:|:----------------:|:----------------:|
| 8+  | :material-check: | :material-check: | :material-check: |

#### JVM on Linux

Targeting JVM running on Linux requires a specific version of the libicu dependency. (You will see an error such as
`libLiteCore.so: libicuuc.so.71: cannot open shared object file: No such file or directory` indicating the expected
version.) If the required version isn't available from your distribution's package manager, you can download it from
[GitHub](https://github.com/unicode-org/icu/releases).

## iOS + macOS :fontawesome-brands-apple:

Kotbase supports native iOS and macOS apps via the [Couchbase Lite Objective-C SDK](
https://github.com/couchbase/couchbase-lite-ios). Developers with experience using Couchbase Lite in Swift should find
Kotbase's API in Kotlin familiar.

Binaries need to link with the correct version of the `CouchbaseLite` XCFramework, which can be downloaded [here](
https://www.couchbase.com/downloads/?family=couchbase-lite) or added via [Carthage or CocoaPods](
https://docs.couchbase.com/couchbase-lite/current/objc/gs-install.html#lbl-install-tabs). The version should match the
major and minor version of Kotbase, e.g. CouchbaseLite {{ version_short }}.x for Kotbase {{ version_full }}.

The [Kotlin CocoaPods Gradle plugin](https://kotlinlang.org/docs/native-cocoapods.html) can also be used to generate a
[Podspec](https://guides.cocoapods.org/syntax/podspec.html) for your project that includes the `CouchbaseLite`
dependency. Use `linkOnly = true` to link the dependency without generating Kotlin Objective-C interop:

??? example "CocoaPods plugin"

    === "Enterprise Edition"
    
        ```kotlin title="build.gradle.kts"
        plugins {
            kotlin("multiplatform")
            kotlin("native.cocoapods")
        }
        
        kotlin {
            cocoapods {
                ...
                pod("CouchbaseLite-Enterprise", version = "{{ version_objc }}", linkOnly = true)
            }
        }
        ```

    === "Community Edition"

        ```kotlin title="build.gradle.kts"
        plugins {
            kotlin("multiplatform")
            kotlin("native.cocoapods")
        }

        kotlin {
            cocoapods {
                ...
                pod("CouchbaseLite", version = "{{ version_objc }}", linkOnly = true)
            }
        }
        ```

### iOS

| Version |       x64        |      ARM64       |
|:-------:|:----------------:|:----------------:|
|   10+   | :material-check: | :material-check: |

### macOS

| Version |       x64        |      ARM64       |
|:-------:|:----------------:|:----------------:|
| 10.14+  | :material-check: | :material-check: |

## Linux :fontawesome-brands-linux: + Windows :fontawesome-brands-windows:

Experimental support for Linux and Windows is provided via the [Couchbase Lite C SDK](
https://github.com/couchbase/couchbase-lite-C). Core functionality should be mostly stable, however these platforms have
not been tested in production. There are some tests that have slightly different behavior in a few edge cases and others
that are failing that need further debugging. See comments in tests marked `@IgnoreLinuxMingw` for details.

There are a few Enterprise Edition features that are not implemented in the Couchbase Lite C SDK. Kotbase will
throw an `UnsupportedOperationException` if these APIs are called from these platforms.

Binaries need to link with the correct version of the native platform `libcblite` binary, which can be downloaded
[here](https://docs.couchbase.com/couchbase-lite/current/c/gs-downloads.html) or [here](
https://www.couchbase.com/downloads/?family=couchbase-lite). The version should match the major and minor version of
Kotbase, e.g. libcblite {{ version_short }}.x for Kotbase {{ version_full }}.

### Linux

Linux also [requires](https://github.com/couchbase/couchbase-lite-core#linux) libz, libicu, and libpthread, which may or
may not be installed on your system.

Targeting Linux requires a specific version of the libicu dependency. (You will see an error such as `libLiteCore.so:
libicuuc.so.71: cannot open shared object file: No such file or directory` indicating the expected version.) If the
required version isn't available from your distribution's package manager, you can download it from [GitHub](
https://github.com/unicode-org/icu/releases).

|     Distro      | Version |       x64        |      ARM64       |
|:---------------:|:-------:|:----------------:|:----------------:|
|     Debian      |   9+    | :material-check: | :material-check: |
| Raspberry Pi OS |   10+   |                  | :material-check: |
|     Ubuntu      | 20.04+  | :material-check: | :material-check: |

### Windows

| Version |       x64        |
|:-------:|:----------------:|
|   10+   | :material-check: |
