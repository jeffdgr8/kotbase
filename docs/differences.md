---
hide:
  - toc
---

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
* While not available in the Java SDK, as Java doesn't support operator overloading, [`Fragment` subscript APIs](
  kotlin-extensions.md#fragment-subscripts) are available in Kotbase, similar to [Swift](
  https://docs.couchbase.com/mobile/3.1.4/couchbase-lite-swift/Classes/Fragment.html), [Objective-C](
  https://docs.couchbase.com/mobile/3.1.4/couchbase-lite-objc/Protocols/CBLFragment.html), and [.NET](
  https://docs.couchbase.com/mobile/3.1.3/couchbase-lite-net/api/Couchbase.Lite.IFragment.html).
