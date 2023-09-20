# Kotbase Getting Started

The Getting Started app is a very basic Kotlin Multiplatform app that demonstrates using Kotbase in a shared Kotlin
module with native apps on each of the supported platforms.

The app [shows examples](shared/src/commonMain/kotlin/dev/kotbase/gettingstarted/shared/SharedDbWork.kt) of the
essential Couchbase Lite CRUD operations, including:

* Create a database
* Create a document
* Retrieve a document
* Update a document
* Query documents
* Create and run a replicator

Whilst no exemplar of a real application, it will give you a good idea how to get started using Kotbase and Kotlin
Multiplatform.

## Shared Kotlin + Native UI

This `getting-started` version demonstrates using shared Kotlin code using Kotbase together with native app UIs.

* The Android app uses XML views.
* The iOS app uses SwiftUI.
* The JVM desktop app uses Compose UI.
* The native app uses a CLI on macOS, Linux, and Windows.
  * Two arguments: the "input" value and true/false for whether to run the replicator
