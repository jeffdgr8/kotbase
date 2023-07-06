package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Instant
import kotlin.coroutines.CoroutineContext

/**
 * A Couchbase Lite database.
 */
public expect class Database {

    /**
     * Construct a Database with a given name and the default config.
     * If the database does not yet exist it will be created.
     *
     * @param name The name of the database: May NOT contain capital letters!
     * @throws CouchbaseLiteException if any error occurs during the open operation.
     */
    @Throws(CouchbaseLiteException::class)
    public constructor(name: String)

    /**
     * Construct a Database with a given name and database config.
     * If the database does not yet exist, it will be created, unless the `readOnly` option is used.
     *
     * @param name   The name of the database: May NOT contain capital letters!
     * @param config The database config.
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the open operation.
     */
    @Throws(CouchbaseLiteException::class)
    public constructor(name: String, config: DatabaseConfiguration)

    public companion object {

        /**
         * Gets the logging controller for the Couchbase Lite library to configure the
         * logging settings and add custom logging.
         */
        public val log: Log

        /**
         * Deletes a database of the given name in the given directory.
         *
         * @param name      the database's name
         * @param directory the directory containing the database: the database's parent directory.
         * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
         */
        @Throws(CouchbaseLiteException::class)
        public fun delete(name: String, directory: String? = null)

        /**
         * Checks whether a database of the given name exists in the given directory or not.
         *
         * @param name      the database's name
         * @param directory the path where the database is located.
         * @return true if exists, false otherwise.
         */
        public fun exists(name: String, directory: String? = null): Boolean

        /**
         * Make a copy of a database in a new location.
         *
         * @param path   path to the existing db file
         * @param name   the name of the new DB
         * @param config a config with the new location
         * @throws CouchbaseLiteException on copy failure
         */
        @Throws(CouchbaseLiteException::class)
        public fun copy(path: String, name: String, config: DatabaseConfiguration? = null)
    }

    /**
     * The database name
     */
    public val name: String

    /**
     * The database's absolute path or null if the database is closed.
     */
    public val path: String?

    /**
     * The number of documents in the database, 0 if database is closed.
     */
    public val count: Long

    /**
     * A READONLY copy of the database configuration.
     */
    public val config: DatabaseConfiguration

    /**
     * Gets an existing Document object with the given ID. If the document with the given ID doesn't
     * exist in the database, the value returned will be null.
     *
     * @param id the document ID
     * @return the Document object
     */
    public fun getDocument(id: String): Document?

    /**
     * Saves a document to the database. When write operations are executed
     * concurrently, the last writer will overwrite all other written values.
     * Calling this method is the same as calling the ave(MutableDocument, ConcurrencyControl)
     * method with LAST_WRITE_WINS concurrency control.
     *
     * @param document The document.
     * @throws CouchbaseLiteException on error
     */
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument)

    /**
     * Saves a document to the database. When used with LAST_WRITE_WINS
     * concurrency control, the last write operation will win if there is a conflict.
     * When used with FAIL_ON_CONFLICT concurrency control, save will fail with false value
     *
     * @param document           The document.
     * @param concurrencyControl The concurrency control.
     * @return true if successful. false if the FAIL_ON_CONFLICT concurrency
     * @throws CouchbaseLiteException on error
     */
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean

    /**
     * Saves a document to the database. Conflicts will be resolved by the passed ConflictHandler.
     * When write operations are executed concurrently and if conflicts occur, the conflict handler
     * will be called. Use the conflict handler to directly edit the document to resolve the
     * conflict. When the conflict handler returns 'true', the save method will save the edited
     * document as the resolved document. If the conflict handler returns 'false', the save
     * operation will be canceled with 'false' value returned as the conflict wasn't resolved.
     *
     * @param document        The document.
     * @param conflictHandler A conflict handler.
     * @return true if successful. false if the FAIL_ON_CONFLICT concurrency
     * @throws CouchbaseLiteException on error
     */
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean

    /**
     * Deletes a document from the database. When write operations are executed
     * concurrently, the last writer will overwrite all other written values.
     * Calling this function is the same as calling the delete(Document, ConcurrencyControl)
     * function with LAST_WRITE_WINS concurrency control.
     *
     * @param document The document.
     * @throws CouchbaseLiteException on error
     */
    @Throws(CouchbaseLiteException::class)
    public fun delete(document: Document)

    /**
     * Deletes a document from the database. When used with lastWriteWins concurrency
     * control, the last write operation will win if there is a conflict.
     * When used with FAIL_ON_CONFLICT concurrency control, delete will fail with
     * 'false' value returned.
     *
     * @param document           The document.
     * @param concurrencyControl The concurrency control.
     * @throws CouchbaseLiteException on error
     */
    @Throws(CouchbaseLiteException::class)
    public fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean

    /**
     * Purges the given document from the database. This is more drastic than delete(Document),
     * it removes all traces of the document. The purge will NOT be replicated to other databases.
     *
     * @param document the document to be purged.
     */
    @Throws(CouchbaseLiteException::class)
    public fun purge(document: Document)

    /**
     * Purges the given document id for the document in database. This is more drastic than delete(Document),
     * it removes all traces of the document. The purge will NOT be replicated to other databases.
     *
     * @param id the document ID
     */
    @Throws(CouchbaseLiteException::class)
    public fun purge(id: String)

    /**
     * Sets an expiration date on a document. After this time, the document
     * will be purged from the database.
     *
     * @param id         The ID of the Document
     * @param expiration Nullable expiration timestamp as a Date, set timestamp to null
     *                   to remove expiration date time from doc.
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun setDocumentExpiration(id: String, expiration: Instant?)

    /**
     * Returns the expiration time of the document. null will be returned if there is
     * no expiration time set
     *
     * @param id The ID of the Document
     * @return Date a nullable expiration timestamp of the document or null if time not set.
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getDocumentExpiration(id: String): Instant?

    /**
     * Runs a group of database operations in a batch. Use this when performing bulk write operations
     * like multiple inserts/updates; it saves the overhead of multiple database commits, greatly
     * improving performance.
     *
     * @param work a unit of work that may terminate abruptly (with an exception)
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun <R> inBatch(work: Database.() -> R): R

    /**
     * Adds a change listener for the changes that occur in the database. The changes will be delivered
     * on the main thread for platforms that support it (Android, iOS, macOS, Linux, and Windows).
     * Callbacks are on an arbitrary thread for the JVM platform.
     *
     * @param listener callback
     * @return An opaque listener token object for removing the listener.
     *
     * @see removeChangeListener
     */
    public fun addChangeListener(listener: DatabaseChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the database with a [CoroutineContext] that will be
     * used to launch coroutines the listener will be called on. Coroutines will be launched in a [CoroutineScope]
     * that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see removeChangeListener
     */
    public fun addChangeListener(context: CoroutineContext, listener: DatabaseChangeSuspendListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the database with a [CoroutineScope] that will be used
     * to launch coroutines the listener will be called on. The listener is removed when the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener callback
     */
    public fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener)

    /**
     * Removes the change listener added to the database.
     *
     * @param token returned by a previous call to [addChangeListener] or [addDocumentChangeListener].
     */
    public fun removeChangeListener(token: ListenerToken)

    /**
     * Adds a change listener for the changes that occur to the specified document. The changes will be
     * delivered on the main thread for platforms that support it (Android, iOS, macOS, Linux, and Windows).
     * Callbacks are on an arbitrary thread for the JVM platform.
     *
     * @param id document ID
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see removeChangeListener
     */
    public fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur to the specified document with a [CoroutineContext]
     * that will be used to launch coroutines the listener will be called on. Coroutines will be launched in
     * a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param id document ID
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see removeChangeListener
     */
    public fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken

    /**
     * Adds a change listener for the changes that occur to the specified document with a [CoroutineScope]
     * that will be used to launch coroutines the listener will be called on. The listener is removed when
     * the scope is canceled.
     *
     * @param id document ID
     * @param scope coroutine scope in which the listener will run
     * @param listener callback
     */
    public fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener)

    /**
     * Closes a database.
     * Closing a database will stop all replicators, live queries and all listeners attached to it.
     *
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun close()

    /**
     * Deletes a database.
     * Deleting a database will stop all replicators, live queries and all listeners attached to it.
     * Although attempting to close a closed database is not an error, attempting to delete a closed database is.
     *
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun delete()

    /**
     * Create a SQL++ query.
     *
     * @param query a valid SQL++ query
     * @return the Query object
     */
    @Throws(CouchbaseLiteException::class)
    public fun createQuery(query: String): Query

    /**
     * Get a list of the names of indices on the default collection.
     *
     * @return the list of index names
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun getIndexes(): List<String>

    /**
     * Add an index to the default collection.
     *
     * @param name  index name
     * @param index index description
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, index: Index)

    /**
     * Add an index to the default collection.
     *
     * @param name   index name
     * @param config index configuration
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, config: IndexConfiguration)

    /**
     * Delete the named index from the default collection.
     *
     * @param name name of the index to delete
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun deleteIndex(name: String)

    /**
     * Performs database maintenance.
     */
    @Throws(CouchbaseLiteException::class)
    public fun performMaintenance(type: MaintenanceType): Boolean
}

/**
 * Gets document fragment object by the given document ID.
 *
 * @param key The key.
 */
public operator fun Database.get(key: String): DocumentFragment =
    DocumentFragment(getDocument(key))
