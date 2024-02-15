_Build and run a starter app using Kotbase_

## Introduction

The Getting Started app is a very basic Kotlin Multiplatform app that demonstrates using Kotbase in a shared Kotlin
module with native apps on each of the supported platforms.

You can access the [`getting-started`](https://github.com/jeffdgr8/kotbase/tree/main/examples/getting-started) and
[`getting-started-compose`](https://github.com/jeffdgr8/kotbase/tree/main/examples/getting-started-compose) projects in
the git repository under examples.

!!! abstract "Quick Steps"

    1. Get the project and open it in Android Studio
    2. Build it
    3. Run any of the platform apps
    4. Enter some input and press "Run database work"  
       The log output, in the app's UI or console panel, will show output similar to that in [Figure 1](#figure-1)
    4. That’s it.

<span id='figure-1'>**Figure 1: Example app output**</span>

```
01-13 11:35:03.733 I/SHARED_KOTLIN: Database created: Database{@@0x9645222: 'desktopApp-db'}
01-13 11:35:03.742 I/SHARED_KOTLIN: Collection created: desktopApp-db@@x7fba7630dcb0._default.example-coll
01-13 11:35:03.764 I/DESKTOP_APP: Created document :: 83b6acb4-21ba-4834-aee4-2419dcea1114
01-13 11:35:03.767 I/SHARED_KOTLIN: Retrieved document:
01-13 11:35:03.767 I/SHARED_KOTLIN: Document ID :: 83b6acb4-21ba-4834-aee4-2419dcea1114
01-13 11:35:03.767 I/SHARED_KOTLIN: Learning :: Kotlin
01-13 11:35:03.768 I/DESKTOP_APP: Updated document :: 83b6acb4-21ba-4834-aee4-2419dcea1114
01-13 11:35:03.785 I/SHARED_KOTLIN: Number of rows :: 1
01-13 11:35:03.789 I/SHARED_KOTLIN: Document ID :: 83b6acb4-21ba-4834-aee4-2419dcea1114
01-13 11:35:03.790 I/SHARED_KOTLIN: Document :: {"language":"Kotlin","version":2.0,"platform":"JVM 21.0.1","input":"Hello, Kotbase!"}
```

## Getting Started App

The Getting Started app [shows examples](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/SharedDbWork.kt)
of the essential Couchbase Lite CRUD operations, including:

* Create a database
* Create a collection
* Create a document
* Retrieve a document
* Update a document
* Query documents
* Create and run a replicator

Whilst no exemplar of a real application, it will give you a good idea how to get started using Kotbase and Kotlin
Multiplatform.

## Shared Kotlin + Native UI

The [`getting-started`](https://github.com/jeffdgr8/kotbase/tree/main/examples/getting-started) version demonstrates
using shared Kotlin code using Kotbase together with native app UIs.

The Kotbase database examples are in the `shared` module, which is shared between each of the platform apps.

### Android App :fontawesome-brands-android:

The Android app is in the `androidApp` module. It uses XML views for its UI.

??? info "Run"

    === "Android Studio"
    
        Run the `androidApp` run configuration.
    
    === "Command Line"
    
        ```title="Install"
        ./gradlew :androidApp:installDebug
        ```
        ```title="Run"
        adb shell am start -n dev.kotbase.gettingstarted/.MainActivity
        ```

### iOS App :fontawesome-brands-apple:

The iOS app is in the `iosApp` directory. It is an Xcode project and uses SwiftUI for its UI.

??? info "Run"

    === "Android Studio"
    
        With the [Kotlin Multiplatform Mobile plugin](
        https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) run the `iosApp` run configuration.
    
    === "Xcode"
    
        Open `iosApp/iosApp.xcodeproj` and run the `iosApp` scheme.

### JVM Desktop App :fontawesome-brands-java:

The JVM desktop app is in the `desktopApp` module. It uses Compose UI for its UI.

??? info "Run"

    === "Android Studio"
    
        Run the `desktopApp` run configuration.
    
    === "Command Line"
    
        ```
        ./gradlew :desktopApp:run
        ```

### Native CLI App :fontawesome-brands-apple::fontawesome-brands-linux::fontawesome-brands-windows:

The native app is in the `cliApp` module. It uses a command-line interface (CLI) on macOS, Linux, and Windows.

The app takes two command-line arguments, first the "input" value, written to the document on update, and second true or
false for whether to run the replicator. These arguments can also be passed as gradle properties.

??? info "Run"

    === "Android Studio"
    
        Run the `cliApp` run configuration.
    
    === "Command Line"
    
        ```
        ./gradlew :cliApp:runDebugExecutableNative -PinputValue="" -Preplicate=false
        ```
        or
        ```title="Build"
        ./gradlew :cliApp:linkDebugExecutableNative
        ```
        ```title="Run"
        cliApp/build/bin/native/debugExecutable/cliApp.kexe "<input value>" <true|false>
        ```

## Share Everything in Kotlin

The [`getting-started-compose`](https://github.com/jeffdgr8/kotbase/tree/main/examples/getting-started-compose) version
demonstrates sharing the entirety of the application code in Kotlin, including the UI with [Compose Multiplatform](
https://www.jetbrains.com/lp/compose-multiplatform/).

The entire compose app is a single Kotlin multiplatform module, encompassing all platforms, with an additional Xcode
project for the iOS app.

### Android App :fontawesome-brands-android:

??? info "Run"

    === "Android Studio"
    
        Run the `androidApp` run configuration.
    
    === "Command Line"
    
        ```title="Install"
        ./gradlew :composeApp:installDebug
        ```
        ```title="Start"
        adb shell am start -n dev.kotbase.gettingstarted.compose/.MainActivity
        ```

### iOS App :fontawesome-brands-apple:

??? info "Run"

    === "Android Studio"
    
        With the [Kotlin Multiplatform Mobile plugin](
        https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) run the `iosApp` run configuration.
    
    === "Xcode"
    
        Open `iosApp/iosApp.xcworkspace` and run the `iosApp` scheme.

!!! important

    Be sure to open `iosApp.xcworkspace` and not `iosApp.xcodeproj`. The `getting-started-compose` `iosApp` uses
    [CocoaPods](https://cocoapods.org/) and the [CocoaPods Gradle plugin](
    https://kotlinlang.org/docs/native-cocoapods.html) to add the `shared` library dependency. The `.xcworkspace`
    includes the CocoaPods dependencies.

!!! note

    Compose Multiplatform [no longer requires CocoaPods](
    https://blog.jetbrains.com/kotlin/2023/08/compose-multiplatform-1-5-0-release/) for copying resources since version
    1.5.0. However, the `getting-started-compose` example still [uses CocoaPods for linking the Couchbase Lite
    framework](
    https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started-compose/shared/build.gradle.kts#L23-L26).
    See the [`getting-started`](
    https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/build.gradle.kts#L14-L30) version for
    an example of how to link the Couchbase Lite framework without using CocoaPods.

### JVM Desktop App :fontawesome-brands-java:

??? info "Run"

    === "Android Studio"
    
        Run the `desktopApp` run configuration.
    
    === "Command Line"
    
        ```
        ./gradlew :composeApp:run
        ```

## Sync Gateway Replication

Using the apps with Sync Gateway and Couchbase Server obviously requires you have, or install, working versions of both.
See also — [Install Sync Gateway](https://docs.couchbase.com/sync-gateway/current/get-started-install.html)

Once you have Sync Gateway configured, update the `ReplicatorConfiguration` [in the app](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/dev/kotbase/gettingstarted/shared/SharedDbWork.kt#L90-L93)
with the server's URL endpoint and authentication credentials.

## Kotlin Multiplatform Tips

### Calling Platform-specific APIs

The apps utilize the Kotlin Multiplatform [`expect`/`actual` feature](
https://kotlinlang.org/docs/multiplatform-connect-to-apis.html) to populate the created document with [the platform](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/SharedDbWork.kt#L36)
the app is running on.

See common [`expect fun getPlatform()`](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/Platform.kt)
and `actual fun getPlatform()` for [Android](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/androidMain/kotlin/Platform.android.kt),
[iOS](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/iosMain/kotlin/Platform.ios.kt),
[JVM](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/jvmMain/kotlin/Platform.jvm.kt),
[Linux](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/linuxMain/kotlin/Platform.linux.kt),
[macOS](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/macosMain/kotlin/Platform.macos.kt),
and [Windows](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/mingwX64Main/kotlin/Platform.mingwX64.kt).

### Using Coroutines in Swift

The `getting-started` app uses [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) to consume
Kotlin `Flow`s in Swift. See [`@NativeCoroutines` annotation](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/SharedDbWork.kt#L91)
in Kotlin and [`asyncSequence(for:)`](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/iosApp/iosApp/ContentView.swift#L99) in Swift
code.

## Going Further

For an example of a full-featured MVVM architected Kotlin Multiplatform app, see the [Kotbase Notes app](
https://github.com/jeffdgr8/kotbase/tree/main/examples/kotbase-notes). This example includes:

* Support for Android, iOS, and JVM desktop platforms.
* Shared data, domain, presentation, and UI logic.
* Platform-specific utility functions via `expect`/`actual`.
* Platform-specific lifecycle management for data sync.
* Dependency injection via [Koin](https://github.com/InsertKoinIO/koin).
* JSON serialization via [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization).
* HTTP client via [Ktor](https://github.com/ktorio/ktor).
* Enhanced Swift interoperability via [SKIE](https://github.com/touchlab/SKIE).

## Kotbase Library Source

The apps can get the Kotbase library dependency either from its published Maven artifact or build the library locally
from the source repository. Set the `useLocalLib` property in **gradle.properties** to `true` to build the library from
source, otherwise the published artifact from Maven Central will be used.
