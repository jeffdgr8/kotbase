_Couchbase Lite — Kotlin support_

## Introduction

In addition to implementing the full Couchbase Lite Java SDK API, Kotbase also provides some additional APIs available
in the [Couchbase Lite Android KTX SDK](https://docs.couchbase.com/couchbase-lite/current/android/kotlin.html), which
includes a number of Kotlin-specific extensions.

This includes:

* [Change Flows](#change-flows) that monitor key Couchbase Lite objects for change using Kotlin features such as, [coroutines](
  https://kotlinlang.org/docs/coroutines-guide.html) and [Flows](https://kotlinlang.org/docs/flow.html).

!!! note

    The configuration factory APIs from the Couchbase Lite Android KTX SDK have been deprecated in Kotbase in favor of
    using constructors directly, which support Kotlin named arguments themselves, or properties can be accessed using
    the `apply` scope function. These APIs will be removed in a future release.

Additionally, while not available in the Java SDK, as Java doesn't support operator overloading, Kotbase adds support
for [`Fragment` subscript APIs](#fragment-subscripts), similar to Couchbase Lite [Swift](
https://docs.couchbase.com/mobile/3.1.10/couchbase-lite-swift/Classes/Fragment.html), [Objective-C](
https://docs.couchbase.com/mobile/3.1.10/couchbase-lite-objc/Protocols/CBLFragment.html), and [.NET](
https://docs.couchbase.com/mobile/3.1.10/couchbase-lite-net/api/Couchbase.Lite.IFragment.html).

## Change Flows

These wrappers use [Flows](https://kotlinlang.org/docs/flow.html) to monitor for changes.

### Collection Change Flow

Use the [`Collection.collectionChangeFlow()`](/api/couchbase-lite-ee/kotbase/collection-change-flow.html) to monitor
collection change events.

=== "In Use"

    ```kotlin
    scope.launch {
        collection.collectionChangeFlow()
            .map { it.documentIDs }
            .collect { docIds: List<String> ->
                // handle changes
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Collection.collectionChangeFlow(
        coroutineContext: CoroutineContext? = null
    ): Flow<CollectionChange>
    ```

### Document Change Flow

Use [`Collection.documentChangeFlow()`](/api/couchbase-lite-ee/kotbase/document-change-flow.html) to monitor changes to a document.

=== "In Use"

    ```kotlin
    scope.launch {
        collection.documentChangeFlow("1001")
            .map { it.collection.getDocument(it.documentID)?.getString("lastModified") }
            .collect { lastModified: String? ->
                // handle document changes
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Collection.documentChangeFlow(
        documentId: String, 
        coroutineContext: CoroutineContext? = null
    ): Flow<DocumentChange>
    ```

### Replicator Change Flow

Use [`Replicator.replicatorChangeFlow()`](/api/couchbase-lite-ee/kotbase/replicator-changes-flow.html) to monitor
replicator changes.

=== "In Use"

    ```kotlin
    scope.launch {
        repl.replicatorChangesFlow()
            .map { it.status.activityLevel }
            .collect { activityLevel: ReplicatorActivityLevel ->
                // handle replicator changes
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Replicator.replicatorChangesFlow(
        coroutineContext: CoroutineContext? = null
    ): Flow<ReplicatorChange>
    ```

### Document Replicator Change Flow

Use [`Replicator.documentReplicationFlow()`]() to monitor document changes during replication.

=== "In Use"

    ```kotlin
    scope.launch {
        repl.documentReplicationFlow()
            .map { it.documents }
            .collect { docs: List<ReplicatedDocument> ->
                // handle replicated documents
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Replicator.documentReplicationFlow(
        coroutineContext: CoroutineContext? = null
    ): Flow<DocumentReplication>
    ```

### Query Change Flow

Use [`Query.queryChangeFlow()`](/api/couchbase-lite-ee/kotbase/query-change-flow.html) to monitor changes to a query.

=== "In Use"

    ```kotlin
    scope.launch {
        query.queryChangeFlow()
            .mapNotNull { change ->
                val err = change.error
                if (err != null) {
                    throw err
                }
                change.results?.allResults()
            }
            .collect { results: List<Result> ->
                // handle query results
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Query.queryChangeFlow(
        coroutineContext: CoroutineContext? = null
    ): Flow<QueryChange>
    ```

## Fragment Subscripts

Kotbase uses Kotlin's [indexed access operator](
https://kotlinlang.org/docs/operator-overloading.html#indexed-access-operator) to implement Couchbase Lite's
[`Fragment`](/api/couchbase-lite-ee/kotbase/-fragment/) subscript APIs for `Database`, `Collection`, `Document`,
`Array`, `Dictionary`, and `Result`, for concise, type-safe, and null-safe access to arbitrary values in a nested JSON
object. `MutableDocument`, `MutableArray`, and `MutableDictionary` also support the [`MutableFragment`](
/api/couchbase-lite-ee/kotbase/-mutable-fragment/) APIs for mutating values.

Supported types can [get `Fragment` or `MutableFragment`](/api/couchbase-lite-ee/kotbase/get.html) objects by either
index or key. `Fragment` objects represent an arbitrary entry in a key path, themselves supporting subscript access to
nested values.

Finally, the typed optional value at the end of a key path can be accessed or set with the [`Fragment`](
/api/couchbase-lite-ee/kotbase/-fragment/) properties, e.g. `array`, `dictionary`, `string`, `int`, `date`, etc.

!!! example "Subscript API examples"

    ```kotlin
    val db = Database("db")
    val coll = db.defaultCollection
    val doc = coll["doc-id"]       // DocumentFragment
    doc.exists                     // true or false
    doc.document                   // "doc-id" Document from Database
    doc["array"].array             // Array value from "array" key
    doc["array"][0].string         // String value from first Array item
    doc["dict"].dictionary         // Dictionary value from "dict" key
    doc["dict"]["num"].int         // Int value from Dictionary "num" key
    coll["milk"]["exp"].date       // Instant value from "exp" key from "milk" Document
    val newDoc = MutableDocument("new-id")
    newDoc["name"].value = "Sally" // set "name" value
    ```
