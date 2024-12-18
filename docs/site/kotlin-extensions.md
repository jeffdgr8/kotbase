_Couchbase Lite — Kotlin support_

## Introduction

In addition to implementing the full Couchbase Lite Java SDK API, Kotbase also provides the additional APIs available in
the [Couchbase Lite Android KTX SDK](https://docs.couchbase.com/couchbase-lite/current/android/kotlin.html), which
includes a number of Kotlin-specific extensions.

This includes:

* [Configuration factories](#configuration-factories) for the configuration of important Couchbase Lite objects such as
  _Databases_, _Replicators_, and _Listeners_.
* [Change Flows](#change-flows) that monitor key Couchbase Lite objects for change using Kotlin features such as, [coroutines](
  https://kotlinlang.org/docs/coroutines-guide.html) and [Flows](https://kotlinlang.org/docs/flow.html).

Additionally, while not available in the Java SDK, as Java doesn't support operator overloading, Kotbase adds support
for [`Fragment` subscript APIs](#fragment-subscripts), similar to Couchbase Lite [Swift](
https://docs.couchbase.com/mobile/3.1.9/couchbase-lite-swift/Classes/Fragment.html), [Objective-C](
https://docs.couchbase.com/mobile/3.1.9/couchbase-lite-objc/Protocols/CBLFragment.html), and [.NET](
https://docs.couchbase.com/mobile/3.1.9/couchbase-lite-net/api/Couchbase.Lite.IFragment.html).

## Configuration Factories

Couchbase Lite provides a set of [configuration factories](/api/couchbase-lite-ee/kotbase/new-config.html). These allow
use of named parameters to specify property settings.

This makes it simple to create variant configurations, by simply overriding named parameters:

!!! example "Example of overriding configuration"

    ```kotlin
    val listener8080 = URLEndpointListenerConfigurationFactory.newConfig(
        networkInterface = "en0",
        port = 8080
    )
    val listener8081 = listener8080.newConfig(port = 8081)
    ```

### Database

Use [`DatabaseConfigurationFactory`](/api/couchbase-lite-ee/kotbase/-database-configuration-factory.html) to create a
[`DatabaseConfiguration`](/api/couchbase-lite-ee/kotbase/-database-configuration/) object, overriding the receiver’s
values with the passed parameters.

=== "In Use"

    ```kotlin
    val database = Database(
        "getting-started",
        DatabaseConfigurationFactory.newConfig()
    )
    ```

=== "Definition"

    ```kotlin
    val DatabaseConfigurationFactory: DatabaseConfiguration? = null
    
    fun DatabaseConfiguration?.newConfig(
        databasePath: String? = null, 
        encryptionKey: EncryptionKey? = null
    ): DatabaseConfiguration
    ```

### Replication

Use [`ReplicatorConfigurationFactory`](/api/couchbase-lite-ee/kotbase/-replicator-configuration-factory.html) to create
a [`ReplicatorConfiguration`](/api/couchbase-lite-ee/kotbase/-replicator-configuration/) object, overriding the
receiver’s values with the passed parameters.

=== "In Use"

    ```kotlin
    val replicator = Replicator(
        ReplicatorConfigurationFactory.newConfig(
            collections = mapOf(db.collections to null),
            target = URLEndpoint("ws://localhost:4984/getting-started-db"),
            type = ReplicatorType.PUSH_AND_PULL,
            authenticator = BasicAuthenticator("sync-gateway", "password".toCharArray())
        )
    )
    ```

=== "Definition"

    ```kotlin
    val ReplicatorConfigurationFactory: ReplicatorConfiguration? = null

    public fun ReplicatorConfiguration?.newConfig(
        target: Endpoint? = null,
        collections: Map<out kotlin.collections.Collection<Collection>, CollectionConfiguration?>? = null,
        type: ReplicatorType? = null,
        continuous: Boolean? = null,
        authenticator: Authenticator? = null,
        headers: Map<String, String>? = null,
        pinnedServerCertificate: ByteArray? = null,
        maxAttempts: Int? = null,
        maxAttemptWaitTime: Int? = null,
        heartbeat: Int? = null,
        enableAutoPurge: Boolean? = null,
        acceptOnlySelfSignedServerCertificate: Boolean? = null,
        acceptParentDomainCookies: Boolean? = null
    ): ReplicatorConfiguration
    ```

### Full Text Search

Use [`FullTextIndexConfigurationFactory`](/api/couchbase-lite-ee/kotbase/-full-text-index-configuration-factory.html) to
create a [`FullTextIndexConfiguration`](/api/couchbase-lite-ee/kotbase/-full-text-index-configuration/) object,
overriding the receiver’s values with the passed parameters.

=== "In Use"

    ```kotlin
    collection.createIndex(
        "overviewFTSIndex",
        FullTextIndexConfigurationFactory.newConfig("overview")
    )
    ```

=== "Definition"

    ```kotlin
    val FullTextIndexConfigurationFactory: FullTextIndexConfiguration? = null
    
    fun FullTextIndexConfiguration?.newConfig(
        vararg expressions: String = emptyArray(), 
        language: String? = null, 
        ignoreAccents: Boolean? = null
    ): FullTextIndexConfiguration
    ```

### Indexing

Use [`ValueIndexConfigurationFactory`](/api/couchbase-lite-ee/kotbase/-value-index-configuration-factory.html) to create
a [`ValueIndexConfiguration`](/api/couchbase-lite-ee/kotbase/-value-index-configuration/) object, overriding the
receiver’s values with the passed parameters.

=== "In Use"

    ```kotlin
    collection.createIndex(
        "TypeNameIndex",
        ValueIndexConfigurationFactory.newConfig("type", "name")
    )
    ```

=== "Definition"

    ```kotlin
    val ValueIndexConfigurationFactory: ValueIndexConfiguration? = null
    
    fun ValueIndexConfiguration?.newConfig(vararg expressions: String = emptyArray()): ValueIndexConfiguration
    ```

### Logs

Use [`LogFileConfigurationFactory`](/api/couchbase-lite-ee/kotbase/-log-file-configuration-factory.html) to create a
[`LogFileConfiguration`](/api/couchbase-lite-ee/kotbase/-log-file-configuration/) object, overriding the receiver’s
values with the passed parameters.

=== "In Use"

    ```kotlin
    Database.log.file.apply {
        config = LogFileConfigurationFactory.newConfig(
            directory = "path/to/temp/logs",
            maxSize = 10240,
            maxRotateCount = 5,
            usePlainText = false
        )
        level = LogLevel.INFO
    }
    ```

=== "Definition"

    ```kotlin
    val LogFileConfigurationFactory: LogFileConfiguration? = null
    
    fun LogFileConfiguration?.newConfig(
        directory: String? = null,
        maxSize: Long? = null,
        maxRotateCount: Int? = null,
        usePlainText: Boolean? = null
    ): LogFileConfiguration
    ```

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
