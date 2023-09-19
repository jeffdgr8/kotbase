In addition to implementing the full Couchbase Lite Java SDK API, Kotbase also provides the additional APIs available in
the [Couchbase Lite Android KTX SDK](https://docs.couchbase.com/couchbase-lite/current/android/kotlin.html), which
includes a number of Kotlin-specific extensions.

This includes:

* [Configuration factories](#configuration-factories) for the configuration of important Couchbase Lite objects such as
  _Databases_, _Replicators_, and _Listeners_.
* [Change Flows](#flows) that monitor key Couchbase Lite objects for change using Kotlin features such as, [coroutines](
  https://kotlinlang.org/docs/coroutines-guide.html) and [Flows](https://kotlinlang.org/docs/flow.html).

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
            database = database,
            target = URLEndpoint("ws://localhost:4984/getting-started-db"),
            type = ReplicatorType.PUSH_AND_PULL,
            authenticator = BasicAuthenticator("sync-gateway", "password".toCharArray())
        )
    )
    ```

=== "Definition"

    ```kotlin
    val ReplicatorConfigurationFactory: ReplicatorConfiguration? = null
    
    fun ReplicatorConfiguration?.newConfig(
        database: Database? = null,
        target: Endpoint? = null,
        type: ReplicatorType? = null,
        continuous: Boolean? = null,
        authenticator: Authenticator? = null,
        headers: Map<String, String>? = null,
        pinnedServerCertificate: ByteArray? = null,
        channels: List<String>? = null,
        documentIDs: List<String>? = null,
        pushFilter: ReplicationFilter? = null,
        pullFilter: ReplicationFilter? = null,
        conflictResolver: ConflictResolver? = null,
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
    database.createIndex(
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
    database.createIndex(
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

## Flows

These wrappers use [Flows](https://kotlinlang.org/docs/flow.html) to monitor for changes.

### Database Change Flow

Use the [`Database.databaseChangeFlow()`](/api/couchbase-lite-ee/kotbase/database-change-flow.html) to monitor database
change events.

=== "In Use"

    ```kotlin
    scope.launch {
        database.databaseChangeFlow()
            .map { it.documentIDs }
            .collect { docIds: List<String> ->
                // handle changes
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Database.databaseChangeFlow(
        coroutineContext: CoroutineContext? = null
    ): Flow<DatabaseChange>
    ```

### Document Change Flow

Use [`Database.documentChangeFlow()`]() to monitor changes to a document.

=== "In Use"

    ```kotlin
    scope.launch {
        database.documentChangeFlow("1001")
            .map { it.database.getDocument(it.documentID)?.getString("lastModified") }
            .collect { lastModified: String? ->
                // handle document changes
            }
    }
    ```

=== "Definition"

    ```kotlin
    fun Database.documentChangeFlow(
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
