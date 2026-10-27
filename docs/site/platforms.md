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

Kotbase links Couchbase Lite using Kotlin's [Swift Package Manager import](
https://kotl.in/cocoapods-to-swiftpm-migration), which requires Kotlin 2.4 or later. The `couchbase-lite` and
`couchbase-lite-ee` artifacts declare the official [Couchbase Lite Swift package](
https://docs.couchbase.com/couchbase-lite/current/swift/gs-install.html) for their edition as a dependency, so Gradle
resolves the matching Couchbase Lite version for you. The Enterprise Edition also includes the Vector Search extension
package.

Your Xcode project needs a one-time setup to embed your Kotlin framework and link these Swift packages. Run the
following from your project root, replacing the Xcode project path and the Gradle project of your shared module:

```shell
XCODEPROJ_PATH='/path/to/iosApp.xcodeproj' GRADLE_PROJECT_PATH=':shared' \
    ./gradlew :shared:integrateEmbedAndSign :shared:integrateLinkagePackage
```

!!! warning

    The [Kotlin CocoaPods Gradle plugin](
    https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-cocoapods-overview.html) can't be used with
    dependencies that declare Swift packages. If your project uses CocoaPods, follow the [CocoaPods to SwiftPM
    migration guide](https://kotl.in/cocoapods-to-swiftpm-migration) first.

### iOS

| Version |      ARM64       | Simulator ARM64  |
|:-------:|:----------------:|:----------------:|
|   15+   | :material-check: | :material-check: |

### macOS

| Version |      ARM64       |
|:-------:|:----------------:|
|   13+   | :material-check: |

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

#### Using APT

Using the Advanced Package Tool (apt) is the easiest way to install Couchbase Lite on Ubuntu and Debian platforms. Just
download the meta package that apt requires to automatically get and install Couchbase Lite, including any dependencies.

1. Download the meta package

    === "curl"

        ```
        curl -O https://packages.couchbase.com/releases/couchbase-release/couchbase-release-1.0-noarch.deb
        ```

    === "wget"

        ```
        wget https://packages.couchbase.com/releases/couchbase-release/couchbase-release-1.0-noarch.deb
        ```

2. Install the meta package

    === "apt"

        ```
        sudo apt install ./couchbase-release-1.0-noarch.deb
        ```

    === "dpkg"

        ```
        sudo dpkg -i ./couchbase-release-1.0-noarch.deb
        ```

3. Update the local package database

    ```
    sudo apt update
    ```

4. Install the required release package(s)

    === "Enterprise"

        ```title="Runtime Only"
        sudo apt install libcblite
        ```
        ```title="Development"
        sudo apt install libcblite-dev
        ```

    === "Community"

        ```title="Runtime Only"
        sudo apt install libcblite-community
        ```
        ```title="Development"
        sudo apt install libcblite-dev-community
        ```

### Windows

| Version |       x64        |
|:-------:|:----------------:|
|   10+   | :material-check: |
