Build and run a starter app using Kotbase

## Introduction

The Getting Started app is a very basic Kotlin Multiplatform app that demonstrates using Kotbase in a shared Kotlin
module with native apps on each of the supported platforms.

You can access the [`getting-started`](https://github.com/jeffdgr8/kotbase/tree/main/examples/getting-started) and
[`getting-started-compose`](https://github.com/jeffdgr8/kotbase/tree/main/examples/getting-started-compose) projects in
the git repository under examples.

## Getting Started App

The Getting Started app [shows examples](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/dev/kotbase/gettingstarted/shared/SharedDbWork.kt)
of the essential Couchbase Lite CRUD operations, including:

* Create database
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

### Android App :fontawesome-brands-android:

The Android app uses XML views.

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

The iOS app uses SwiftUI.

??? info "Run"

    === "Android Studio"
    
        With the [Kotlin Multiplatform Mobile plugin](
        https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) run the `iosApp` run configuration.
    
    === "Xcode"
    
        Open `iosApp/iosApp.xcodeproj` and run the `iosApp` scheme.

### JVM Desktop App :fontawesome-brands-java:

The JVM desktop app uses Compose UI.

??? info "Run"

    === "Android Studio"
    
        Run the `desktopApp` run configuration.
    
    === "Command Line"
    
        ```
        ./gradlew :desktopApp:run
        ```

### Native CLI App :fontawesome-brands-apple::fontawesome-brands-linux::fontawesome-brands-windows:

The native app uses a command-line interface (CLI) on macOS, Linux, and Windows.

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

### Android App :fontawesome-brands-android:

??? info "Run"

    === "Android Studio"
    
        Run the `androidApp` run configuration.
    
    === "Command Line"
    
        ```title="Install"
        ./gradlew :androidApp:installDebug
        ```
        ```title="Start"
        adb shell am start -n dev.kotbase.gettingstarted/.MainActivity
        ```

### iOS App :fontawesome-brands-apple:

??? info "Run"

    === "Android Studio"
    
        With the [Kotlin Multiplatform Mobile plugin](
        https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) run the `iosApp` run configuration.
    
    === "Xcode"
    
        Open `iosApp/iosApp.xcworkspace` and run the `iosApp` scheme.

??? warning "Open iosApp.xcworkspace"

    Be sure to open `iosApp.xcworkspace` and not `iosApp.xcodeproj`. The `iosApp` uses [CocoaPods](
    https://cocoapods.org/) and the [CocoaPods Gradle plugin](https://kotlinlang.org/docs/native-cocoapods.html) to add
    the `shared` library dependency. The `.xcworkspace` includes the CocoaPods dependencies.

### JVM Desktop App :fontawesome-brands-java:

??? info "Run"

    === "Android Studio"
    
        Run the `desktopApp` run configuration.
    
    === "Command Line"
    
        ```
        ./gradlew :desktopApp:run
        ```

## Sync Gateway Replication

Using the apps with Sync Gateway and Couchbase Server obviously requires you have, or install, working versions of both.
See also — [Install Sync Gateway](https://docs.couchbase.com/sync-gateway/current/get-started-install.html)

## Kotlin Multiplatform Tips

### Calling Platform-specific APIs

The apps utilize the Kotlin Multiplatform [`expect`/`actual` feature](
https://kotlinlang.org/docs/multiplatform-connect-to-apis.html) to populate the created document with [the platform](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/dev/kotbase/gettingstarted/shared/SharedDbWork.kt#L29)
the app is running on.

See common [`expect class Platform`](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/dev/kotbase/gettingstarted/shared/Platform.kt)
and `actual class Platform` for [Android](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/androidMain/kotlin/dev/kotbase/gettingstarted/shared/Platform.android.kt),
[iOS](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/iosMain/kotlin/dev/kotbase/gettingstarted/shared/Platform.ios.kt),
[JVM](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/jvmMain/kotlin/dev/kotbase/gettingstarted/shared/Platform.jvm.kt),
[Linux](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/linuxX64Main/kotlin/dev/kotbase/gettingstarted/shared/Platform.linuxX64.kt),
[macOS](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/macosMain/kotlin/dev/kotbase/gettingstarted/shared/Platform.macos.kt),
and [Windows](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/mingwX64Main/kotlin/dev/kotbase/gettingstarted/shared/Platform.mingwX64.kt).

### Using Coroutines in Swift

The `getting-started` app uses [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) to consume
Kotlin `Flow`s in Swift. See [`@NativeCoroutines` annotation](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/shared/src/commonMain/kotlin/dev/kotbase/gettingstarted/shared/SharedDbWork.kt#L84)
in Kotlin and [`asyncSequence(for:)`](
https://github.com/jeffdgr8/kotbase/blob/main/examples/getting-started/iosApp/iosApp/ContentView.swift#L97) in Swift
code.
