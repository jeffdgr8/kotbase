package com.couchbase.lite.kmm

import kotlinx.datetime.Instant

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
        public fun delete(name: String, directory: String?)

        /**
         * Checks whether a database of the given name exists in the given directory or not.
         *
         * @param name      the database's name
         * @param directory the path where the database is located.
         * @return true if exists, false otherwise.
         */
        public fun exists(name: String, directory: String): Boolean

        /**
         * Make a copy of a database in a new location.
         *
         * @param path   path to the existing db file
         * @param name   the name of the new DB
         * @param config a config with the new location
         * @throws CouchbaseLiteException on copy failure
         */
        @Throws(CouchbaseLiteException::class)
        public fun copy(path: String, name: String, config: DatabaseConfiguration)
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
     * Saves a document to the database. Conflicts will be resolved by the passed ConflictHandler
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
    public fun inBatch(work: () -> Unit)

    /**
     * Adds a change listener for the changes that occur in the database. The changes will be delivered on the UI
     * thread for the Android platform and on an arbitrary thread for the Java platform. When developing a Java
     * Desktop application using Swing or JavaFX that needs to update the UI after receiving the changes, make
     * sure to schedule the UI update on the UI thread by using SwingUtilities.invokeLater(Runnable) or
     * Platform.runLater(Runnable) respectively.
     *
     * @param listener callback
     */
    public fun addChangeListener(listener: DatabaseChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the database with an executor on which the changes will be
     * posted to the listener. If the executor is not specified, the changes will be delivered on the UI thread for
     * the Android platform and on an arbitrary thread for the Java platform.
     *
     * @param listener callback
     */
    // TODO:
    //public fun addChangeListener(executor: Executor?, listener: DatabaseChangeListener): ListenerToken

    /**
     * Removes the change listener added to the database.
     *
     * @param token returned by a previous call to addChangeListener or addDocumentListener.
     */
    public fun removeChangeListener(token: ListenerToken)

    /**
     * Adds a change listener for the changes that occur to the specified document.
     * The changes will be delivered on the UI thread for the Android platform and on an arbitrary
     * thread for the Java platform. When developing a Java Desktop application using Swing or JavaFX
     * that needs to update the UI after receiving the changes, make sure to schedule the UI update
     * on the UI thread by using SwingUtilities.invokeLater(Runnable) or Platform.runLater(Runnable)
     * respectively.
     */
    public fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur to the specified document with an executor on which
     * the changes will be posted to the listener.  If the executor is not specified, the changes will be
     * delivered on the UI thread for the Android platform and on an arbitrary thread for the Java platform.
     */
    // TODO:
    //public fun addDocumentChangeListener(id: String, executor: Executor?, listener: DocumentChangeListener): ListenerToken

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

    @Throws(CouchbaseLiteException::class)
    public fun createQuery(query: String): Query

    @Throws(CouchbaseLiteException::class)
    public fun getIndexes(): List<String>

    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, index: Index)

    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, config: IndexConfiguration)

    @Throws(CouchbaseLiteException::class)
    public fun deleteIndex(name: String)

    @Throws(CouchbaseLiteException::class)
    public fun performMaintenance(type: MaintenanceType): Boolean
}
