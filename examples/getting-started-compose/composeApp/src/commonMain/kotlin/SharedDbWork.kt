import kotbase.*
import kotbase.Collection
import kotbase.ktx.select
import kotbase.ktx.all
import kotbase.ktx.from
import kotbase.ktx.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class SharedDbWork {

    private var database: Database? = null
    private var collection: Collection? = null
    private var replicator: Replicator? = null

    // Create a database
    fun createDb(dbName: String) {
        database = Database(dbName)
        Log.i(TAG, "Database created: $database")
    }

    // Create a new named collection (like a SQL table)
    // in the database's default scope.
    fun createCollection(collName: String) {
        collection = database!!.createCollection(collName)
        Log.i(TAG, "Collection created: $collection")
    }

    // Create a new document (i.e. a record)
    // and save it in a collection in the database.
    fun createDoc(): String {
        val mutableDocument = MutableDocument()
            .setFloat("version", 2.0f)
            .setString("language", "Kotlin")
            .setString("platform", getPlatform().name)
        collection?.save(mutableDocument)
        return mutableDocument.id
    }

    // Retrieve immutable document and log the database generated
    // document ID and some document properties
    fun retrieveDoc(docId: String) {
        collection?.getDocument(docId)
            ?.let {
                Log.i(TAG, "Retrieved document:")
                Log.i(TAG, "Document ID :: $docId")
                Log.i(TAG, "Learning :: ${it.getString("language")}")
            }
            ?: Log.i(TAG, "No such document :: $docId")
    }

    // Retrieve immutable document and set `input` property
    fun updateDoc(docId: String, inputValue: String) {
        collection?.getDocument(docId)?.let {
            collection?.save(
                it.toMutable().setString("input", inputValue)
            )
        }
    }

    // Create a query to fetch documents with language == Kotlin.
    fun queryDocs() {
        val coll = collection ?: return

        var query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(coll))
            .where(Expression.property("language").equalTo(Expression.string("Kotlin")))
        query.execute().use { rs ->
            Log.i(TAG, "Number of rows :: ${rs.allResults().size}")
        }

        // KTX API
        query = select(Meta.id, all())
            .from(coll)
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
        val coll = collection ?: return emptyFlow()

        val collConfig = CollectionConfiguration(
            pullFilter = { doc, _ -> "Kotlin" == doc.getString("language") }
        )

        val repl = Replicator(
            ReplicatorConfigurationFactory.newConfig(
                target = URLEndpoint("ws://localhost:4984/getting-started-db"),
                collections = mapOf(setOf(coll) to collConfig),
                type = ReplicatorType.PUSH_AND_PULL,
                authenticator = BasicAuthenticator("sync-gateway", "password".toCharArray())
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
