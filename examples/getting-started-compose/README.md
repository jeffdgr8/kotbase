# Kotbase Getting Started Compose Multiplatform

The Getting Started app is a very basic Kotlin Multiplatform app that demonstrates using Kotbase in a shared Kotlin
module with native apps on each of the supported platforms.

The app [shows examples](composeApp/src/commonMain/kotlin/SharedDbWork.kt) of the essential Couchbase Lite CRUD
operations, including:

* Create a database
* Create a collection
* Create a document
* Retrieve a document
* Update a document
* Query documents
* Create and run a replicator

Whilst no exemplar of a real application, it will give you a good idea how to get started using Kotbase and Kotlin
Multiplatform.

## Share Everything in Kotlin

This `getting-started-compose` version demonstrates sharing the entirety of the application code in Kotlin, including
the UI with [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).

The entire compose app is a single Kotlin multiplatform module, encompassing all platforms, with an additional Xcode
project for the iOS app.