[![Maven Central](https://img.shields.io/maven-central/v/dev.kotbase/couchbase-lite)](
https://central.sonatype.com/namespace/dev.kotbase)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/github/license/jeffdgr8/kotbase)](LICENSE)
[![Couchbase Community](https://img.shields.io/badge/couchbase-community-ea2328?logo=couchbase&logoColor=ea2328)](
https://www.couchbase.com/developers/community/)
[![Slack Channel](https://img.shields.io/badge/chat-%23couchbase-4a154b.svg?logo=slack)](
https://kotlinlang.slack.com/messages/couchbase/)

# Kotbase

**Kot**lin Multiplatform library for Couch**base** Lite

## Introduction

[Couchbase Lite](https://www.couchbase.com/products/lite/) is an embedded NoSQL JSON document database. It can be used
as a standalone client database, or paired with [Couchbase Server](https://www.couchbase.com/products/server/) and
[Sync Gateway](https://www.couchbase.com/products/sync-gateway/) or [Capella App Services](
https://www.couchbase.com/products/capella/app-services/) for cloud to edge data synchronization. Features include:

* [SQL++](https://www.couchbase.com/products/n1ql/), key/value, and full-text search queries
* Observable queries, documents, databases, and replicators
* Binary document attachments (blobs)
* Peer-to-peer and cloud-to-edge data sync

Kotbase provides full Enterprise and Community Edition API support for Android and JVM ([via Java SDK](
https://github.com/couchbase/couchbase-lite-java-ce-root)), native iOS and macOS ([via Objective-C SDK](
https://github.com/couchbase/couchbase-lite-ios)), and experimental support for available APIs in native Linux and
Windows ([via C SDK](https://github.com/couchbase/couchbase-lite-C)).

> **Note**
> The Community Edition is free and open source. The Enterprise Edition is free for development and testing, but
> requires a [license](https://www.couchbase.com/pricing/#couchbase-mobile) for production use. [See Community vs
> Enterprise Edition.](https://www.couchbase.com/products/editions/#couchbase_lite)

## Installation

**build.gradle.kts**
```kotlin
kotlin.sourceSets.commonMain {
    dependencies {
        // Community Edition
        implementation("dev.kotbase:couchbase-lite:3.0.5-1.0.0")
        // or Enterprise Edition
        implementation("dev.kotbase:couchbase-lite-ee:3.0.5-1.0.0")
    }
}
```

## Differences from Couchbase Lite Java SDK

Kotbase's API aligns with the [Java](https://docs.couchbase.com/couchbase-lite/current/java/quickstart.html) and
[Android KTX](https://docs.couchbase.com/couchbase-lite/current/android/quickstart.html) SDKs. Migrating existing Kotlin
code can be as straightforward as changing the import package from `com.couchbase.lite` to `kotbase`, with a few
exceptions:

* Java callback functional interfaces are implemented as Kotlin function types.
* `File` path APIs are represented as strings.
* `Date` APIs use [KotlinX Date/Time's `Instant`](
https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/kotlinx.datetime/-instant/).
* `Blob` `InputStream` APIs use [Okio's `Source`](https://square.github.io/okio/3.x/okio/okio/okio/-source/index.html).
* `Executor` APIs are not implemented. Listener callbacks are on the main thread.
* Certificate APIs are available as raw `ByteArray`s or in platform-specific code.
* There's no need to explicitly call `CouchbaseLite.init()`. Initialization functions can still be called with custom
parameters in JVM and Android platform code if required.
* Efforts have been made to detect and throw Kotlin exceptions for common error conditions, but `NSError` may still leak
through on Apple platforms. [Please report](https://github.com/jeffdgr8/kotbase/issues/new) any occurrences that may
deserve addressing.
* Deprecated APIs are omitted.
* While not available in the Java SDK, as Java doesn't support operator overloading, `Fragment` subscript APIs for
`Database`, `Document`, `Array`, and `Dictionary` are available in Kotbase, similar to [Swift](
https://docs.couchbase.com/mobile/3.0.2/couchbase-lite-swift/Classes/Fragment.html), [Objective-C](
https://docs.couchbase.com/mobile/3.0.2/couchbase-lite-objc/Protocols/CBLFragment.html), and [.NET](
https://docs.couchbase.com/mobile/3.0.2/couchbase-lite-net/api/Couchbase.Lite.IFragment.html) APIs.
<details>
<summary>Subscript API examples</summary>

```kotlin
val db = Database("db")
val doc = db["doc-id"]         // DocumentFragment
doc.exists                     // true or false
doc.document                   // "doc-id" Document from Database
doc["array"].array             // Array value from "array" key
doc["array"][0].string         // String value from first Array item
doc["dict"].dictionary         // Dictionary value from "dict" key
doc["dict"]["num"].int         // Int value from Dictionary "num" key
db["milk"]["exp"].date         // Instant value from "exp" key from "milk" Document
val newDoc = MutableDocument("new-id")
newDoc["name"].value = "Sally" // set "name" value
```
</details>

## Extension Libraries

Kotbase includes some convenient extensions on top of Couchbase Lite's official API as additional library artifacts.

### Kotbase KTX

The KTX extensions include the excellent [Kotlin extensions by MOLO17](https://github.com/MOLO17/couchbase-lite-kotlin),
as well as other convenience functions for composing queries, observing change `Flow`s, and creating indexes.

#### Installation

**build.gradle.kts**
```kotlin
// Community Edition
implementation("dev.kotbase:couchbase-lite-ktx:3.0.5-1.0.0")
// or Enterprise Edition
implementation("dev.kotbase:couchbase-lite-ee-ktx:3.0.5-1.0.0")
```

### Kotbase Paging

The paging extensions are built on Cash App's [Multiplatform Paging](https://github.com/cashapp/multiplatform-paging),
Google's [AndroidX Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) with support
for both Android and iOS. Kotbase Paging provides a [`PagingSource`](
https://developer.android.com/reference/kotlin/androidx/paging/PagingSource) which performs limit/offset paging queries
based on a user-supplied database query.

#### Installation

**build.gradle.kts**
```kotlin
// Community Edition
implementation("dev.kotbase:couchbase-lite-paging:3.0.5-1.0.0")
// or Enterprise Edition
implementation("dev.kotbase:couchbase-lite-ee-paging:3.0.5-1.0.0")
```

## Roadmap

* [ ] Public release
* [ ] Documentation website ([kotbase.dev](https://kotbase.dev/))
* [ ] Sample apps
* [ ] Couchbase Lite [3.1 API](https://docs.couchbase.com/couchbase-lite/3.1/cbl-whatsnew.html) - Scopes and Collections
* [ ] Versioned docs
