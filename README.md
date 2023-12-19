[![Maven Central](https://img.shields.io/maven-central/v/dev.kotbase/couchbase-lite)](
https://central.sonatype.com/namespace/dev.kotbase)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/github/license/jeffdgr8/kotbase)](LICENSE)
[![Couchbase Community](https://img.shields.io/badge/couchbase-community-ea2328?logo=couchbase&logoColor=ea2328)](
https://www.couchbase.com/developers/community/)
[![Slack Channel](https://img.shields.io/badge/chat-%23couchbase-4a154b.svg?logo=slack)](
https://kotlinlang.slack.com/messages/couchbase/)

# Kotbase

**Kot**lin Multiplatform library for Couch**base** Lite

## Introduction

Kotbase pairs [Kotlin Multiplatform](https://kotlinlang.org/lp/multiplatform/) with [Couchbase Lite](
https://www.couchbase.com/products/lite/), an embedded NoSQL JSON document database. Couchbase Lite can be used as a
standalone client database, or paired with [Couchbase Server](https://www.couchbase.com/products/server/) and [Sync
Gateway](https://www.couchbase.com/products/sync-gateway/) or [Capella App Services](
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

Add either the Community or Enterprise Edition dependency in the **commonMain** source set dependencies of your
shared module's **build.gradle.kts**:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Community Edition
            implementation("dev.kotbase:couchbase-lite:3.0.15-1.0.1")
            // or Enterprise Edition
            implementation("dev.kotbase:couchbase-lite-ee:3.0.15-1.0.1")
        }
    }
}
```

Kotbase is published to Maven Central. The Couchbase Lite Enterprise Edition dependency additionally requires the
Couchbase Maven repository.

```kotlin
repositories {
    mavenCentral()
    maven("https://mobile.maven.couchbase.com/maven2/dev/")
}
```

### Native Platforms

Native platform targets should additionally link to the Couchbase Lite dependency native binary. See [Supported
Platforms](https://kotbase.dev/platforms/) for more details.

### Linux

Targeting JVM running on Linux or native Linux, both require a specific version of the libicu dependency. (You will see
an error such as `libLiteCore.so: libicuuc.so.71: cannot open shared object file: No such file or directory` indicating
the expected version.) If the required version isn't available from your distribution's package manager, you can
download it from [GitHub](https://github.com/unicode-org/icu/releases).

## Documentation

Kotbase documentation can be found at [kotbase.dev](https://kotbase.dev/), including [getting started examples](
https://kotbase.dev/getting-started/), [usage guide](https://kotbase.dev/databases/), and [API reference](
https://kotbase.dev/api/).

## Differences from Java SDK

Kotbase's API aligns with the Couchbase Lite [Java](
https://docs.couchbase.com/couchbase-lite/current/java/quickstart.html) and [Android KTX](
https://docs.couchbase.com/couchbase-lite/current/android/quickstart.html) SDKs. Migrating existing Kotlin code can be
as straightforward as changing the import package from `com.couchbase.lite` to `kotbase`, with some exceptions:

* Java callback functional interfaces are implemented as Kotlin function types.
* `File`, `URL`, and `URI` APIs are represented as strings.
* `Date` APIs use [kotlinx-datetime's `Instant`](
https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/kotlinx.datetime/-instant/).
* `InputStream` APIs use [kotlinx-io's `Source`](
  https://fzhinkin.github.io/kotlinx-io-dokka-docs-preview/kotlinx-io-core/kotlinx.io/-source/).
* `Executor` APIs use Kotlin's `CoroutineContext`.
* Certificate APIs are available as raw `ByteArray`s or in platform-specific code.
* There's no need to explicitly call `CouchbaseLite.init()`. Initialization functions can still be called with custom
  parameters in JVM and Android platform code.
* Efforts have been made to detect and throw Kotlin exceptions for common error conditions, but `NSError` may still leak
  through on Apple platforms. [Please report](https://github.com/jeffdgr8/kotbase/issues/new) any occurrences that may
  deserve addressing.
* Some deprecated APIs are omitted.
* While not available in the Java SDK, as Java doesn't support operator overloading, `Fragment` subscript APIs are
  available in Kotbase, similar to [Swift](
  https://docs.couchbase.com/mobile/3.0.15/couchbase-lite-swift/Classes/Fragment.html), [Objective-C](
  https://docs.couchbase.com/mobile/3.0.15/couchbase-lite-objc/Protocols/CBLFragment.html), and [.NET](
  https://docs.couchbase.com/mobile/3.0.15/couchbase-lite-net/api/Couchbase.Lite.IFragment.html).

## Extension Libraries

Kotbase includes some convenient extensions on top of Couchbase Lite's official API as additional library artifacts.

### Kotbase KTX

The [KTX extensions](couchbase-lite-ktx/README.md) include the excellent [Kotlin extensions by MOLO17](https://github.com/MOLO17/couchbase-lite-kotlin),
as well as other convenience functions for composing queries, observing change `Flow`s, and creating indexes.

#### Installation

```kotlin
// Community Edition
implementation("dev.kotbase:couchbase-lite-ktx:3.0.15-1.0.1")
// or Enterprise Edition
implementation("dev.kotbase:couchbase-lite-ee-ktx:3.0.15-1.0.1")
```

### Kotbase Kermit

[Kotbase Kermit](couchbase-lite-kermit/README.md) is a Couchbase Lite custom logger which logs to [Kermit](
https://kermit.touchlab.co/). Kermit can direct its logs to any number of log outputs, including the console.

#### Installation

```kotlin
// Community Edition
implementation("dev.kotbase:couchbase-lite-kermit:3.0.15-1.0.1")
// or Enterprise Edition
implementation("dev.kotbase:couchbase-lite-ee-kermit:3.0.15-1.0.1")
```

### Kotbase Paging

The [paging extensions](couchbase-lite-paging/README.md) are built on Cash App's [Multiplatform Paging](
https://github.com/cashapp/multiplatform-paging), Google's [AndroidX Paging](
https://developer.android.com/topic/libraries/architecture/paging/v3-overview) with multiplatform support. Kotbase
Paging provides a [`PagingSource`](https://developer.android.com/reference/kotlin/androidx/paging/PagingSource) which
performs limit/offset paging queries based on a user-supplied database query.

#### Installation

```kotlin
// Community Edition
implementation("dev.kotbase:couchbase-lite-paging:3.0.15-1.0.1")
// or Enterprise Edition
implementation("dev.kotbase:couchbase-lite-ee-paging:3.0.15-1.0.1")
```

## Roadmap

* [x] Documentation website ([kotbase.dev](https://kotbase.dev/))
* [x] `NSInputStream` interoperability ~~(Okio [#1123](https://github.com/square/okio/pull/1123))~~ (kotlinx-io [#174](
  https://github.com/Kotlin/kotlinx-io/pull/174))
* [x] Linux ARM64 support
* [x] Public release
* [ ] Sample apps
    * [x] [Getting Started](examples/getting-started)
    * [x] [Getting Started Compose Multiplatform](examples/getting-started-compose)
* [ ] Couchbase Lite [3.1 API](https://docs.couchbase.com/couchbase-lite/3.1/cbl-whatsnew.html) - Scopes and Collections
* [ ] Versioned docs

## Development

* When building the project on Linux, be sure to install the libicu dependency.
* When checking out the git repo on Windows, enable [developer mode](
  https://learn.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development) and symbolic links in
  git with `git config --global core.symlinks true`.
