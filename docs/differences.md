---
hide:
  - toc
---

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
??? example "Subscript API"

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
