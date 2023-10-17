package dev.kotbase.gettingstarted.shared

import kotbase.*
import kotbase.ktx.select
import kotbase.ktx.all
import kotbase.ktx.from
import kotbase.ktx.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class SharedDbWork {

    private var database: Database? = null
    private var replicator: Replicator? = null
    private val platform: Platform = getPlatform()

    // Create a database
    fun createDb(dbName: String) {
        database = Database(dbName)
        Log.i(TAG, "Database created: $dbName")
    }

    // Create a new document (i.e. a record)
    // and save it in a collection in the database.
    fun createDoc(): String {
        val mutableDoc = MutableDocument()
            .setFloat("version", 2.0f)
            .setString("language", "Kotlin")
            .setString("platform", platform.name)
        database?.save(mutableDoc)
        return mutableDoc.id
    }

    // Retrieve immutable document and log the database generated
    // document ID and some document properties
    fun retrieveDoc(docId: String) {
        database?.getDocument(docId)
            ?.let {
                Log.i(TAG, "Retrieved document:")
                Log.i(TAG, "Document ID :: $docId")
                Log.i(TAG, "Learning :: ${it.getString("language")}")
            }
            ?: Log.i(TAG, "No such document :: $docId")
    }

    // Retrieve immutable document and set `input` property
    fun updateDoc(docId: String, inputValue: String) {
        database?.getDocument(docId)?.let {
            database?.save(
                it.toMutable().setString("input", inputValue)
            )
        }
    }

    // Create a query to fetch documents with language == Kotlin.
    @OptIn(ExperimentalStdlibApi::class)
    fun queryDocs() {
        val database = database ?: return

        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("language").equalTo(Expression.string("Kotlin")))
        query.execute().use { rs ->
            Log.i(TAG, "Number of rows :: ${rs.allResults().size}")
        }

        // KTX API
        query = select(Meta.id, all())
            .from(database)
            .where {
                "language" equalTo "Kotlin"
            }
        query.execute().use { rs ->
            rs.forEach { result ->
                Log.i(TAG, "Document ID :: ${result.getString(0)}")
                Log.i(TAG, "Document :: ${result.getDictionary(1)?.toJSON()}")
            }
        }
    }

    // OPTIONAL -- if you have Sync Gateway Installed you can try replication too.
    // Create a replicator to push and pull changes to and from the cloud.
    // Be sure to hold a reference to the Replicator to prevent it from being GCed
    fun replicate(): Flow<ReplicatorChange> {
        val database = database ?: return emptyFlow()

        val repl = Replicator(
            ReplicatorConfigurationFactory.newConfig(
                target = URLEndpoint("ws://localhost:4984/getting-started-db"),
                database = database,
                type = ReplicatorType.PUSH_AND_PULL,
                authenticator = BasicAuthenticator("sync-gateway", "password".toCharArray()),
                pullFilter = { doc, _ -> "Kotlin" == doc.getString("language") }
            )
        )

        // Listen to replicator change events.
        val changes = repl.replicatorChangesFlow()

        // Start replication.
        repl.start()
        replicator = repl

        return changes
    }

    private companion object {
        private const val TAG = "SHARED_KOTLIN"
    }
}
