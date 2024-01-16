/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Instant
import kotlin.coroutines.CoroutineContext

/**
 * A Couchbase Lite database.
 */
@OptIn(ExperimentalStdlibApi::class)
public expect class Database : AutoCloseable {

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
         * @param directory the path where the database is located. If null, the default db directory will be used.
         * @return true if exists, false otherwise.
         */
        public fun exists(name: String, directory: String? = null): Boolean

        /**
         * Make a copy of a database in a new location.
         * It is recommended that this method not be used on an open database.
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
     * A READONLY copy of the database configuration.
     */
    public val config: DatabaseConfiguration

    /**
     * Closes a database.
     * Closing a database will stop all replicators, live queries and all listeners attached to it.
     *
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    override fun close()

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
     * Get scope names that have at least one collection.
     * Note: the default scope is exceptional as it will always be listed even though there are no collections
     * under it.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val scopes: Set<Scope>

    /**
     * Get a scope object by name. As the scope cannot exist by itself without having a collection,
     * the null value will be returned if there are no collections under the given scope’s name.
     * Note: The default scope is exceptional, and it will always be returned.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getScope(name: String): Scope?

    /**
     * Get the default scope.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val defaultScope: Scope

    /**
     * Create a named collection in the default scope.
     * If the collection already exists, the existing collection will be returned.
     *
     * @param name the scope in which to create the collection
     * @return the named collection in the default scope
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createCollection(name: String): Collection

    /**
     * Create a named collection in the specified scope.
     * If the collection already exists, the existing collection will be returned.
     *
     * @param collectionName the name of the new collection
     * @param scopeName      the scope in which to create the collection
     * @return the named collection in the default scope
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createCollection(collectionName: String, scopeName: String?): Collection

    /**
     * Get all collections in the default scope.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val collections: Set<Collection>

    /**
     * Get all collections in the named scope.
     *
     * @param scopeName the scope name
     * @return the collections in the named scope
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollections(scopeName: String?): Set<Collection>

    /**
     * Get a collection in the default scope by name.
     * If the collection doesn't exist, the function will return null.
     *
     * @param name the collection to find
     * @return the named collection or null
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollection(name: String): Collection?

    /**
     * Get a collection in the specified scope by name.
     * If the collection doesn't exist, the function will return null.
     *
     * @param collectionName the collection to find
     * @param scopeName      the scope in which to create the collection
     * @return the named collection or null
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollection(collectionName: String, scopeName: String?): Collection?

    /**
     * Get the default collection.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val defaultCollection: Collection

    /**
     * Delete a collection by name in the default scope. If the collection doesn't exist, the operation
     * will be no-ops. Note: the default collection cannot be deleted.
     *
     * @param name the collection to be deleted
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun deleteCollection(name: String)

    /**
     * Delete a collection by name in the specified scope. If the collection doesn't exist, the operation
     * will be no-ops. Note: the default collection cannot be deleted.
     *
     * @param collectionName the collection to be deleted
     * @param scopeName      the scope from which to delete the collection
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun deleteCollection(collectionName: String, scopeName: String?)

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
     * Create a SQL++ query.
     *
     * @param query a valid SQL++ query
     * @return the Query object
     */
    public fun createQuery(query: String): Query

    /**
     * The number of documents in the default collection, 0 if database is closed.
     */
    @Deprecated(
        "Use defaultCollection.count",
        ReplaceWith("defaultCollection.count")
    )
    public val count: Long

    /**
     * Gets an existing Document with the given ID from the default collection.
     * If the document with the given ID doesn't exist in the default collection,
     * the method will return null. If the database is closed the method will
     * throw an IllegalStateException.
     *
     * @param id the document ID
     * @return the Document object or null
     * @throws IllegalStateException when the database is closed
     */
    @Deprecated(
        "Use defaultCollection.getDocument()",
        ReplaceWith("defaultCollection.getDocument(id)")
    )
    public fun getDocument(id: String): Document?

    /**
     * Saves a document to the default collection. When write operations are executed
     * concurrently, the last writer will overwrite all other written values.
     * Calling this method is the same as calling save(MutableDocument, ConcurrencyControl.LAST_WRITE_WINS)
     *
     * @param document The document.
     * @throws CouchbaseLiteException on error
     */
    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument)

    /**
     * Saves a document to the default collection. When used with LAST_WRITE_WINS
     * concurrency control, the last write operation will win if there is a conflict.
     * When used with FAIL_ON_CONFLICT concurrency control, save will fail when there
     * is a conflict and the method will return false
     *
     * @param document           The document.
     * @param concurrencyControl The concurrency control.
     * @return true if successful. false if the FAIL_ON_CONFLICT concurrency
     * @throws CouchbaseLiteException on error
     */
    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean

    /**
     * Saves a document to the default collection. Conflicts will be resolved by the passed ConflictHandler
     *
     * @param document        The document.
     * @param conflictHandler A conflict handler.
     * @return true if successful. false if the FAIL_ON_CONFLICT concurrency
     * @throws CouchbaseLiteException on error
     */
    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document, conflictHandler)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean

    /**
     * Deletes a document from the default collection. When write operations are executed
     * concurrently, the last writer will overwrite all other written values.
     * Calling this function is the same as calling delete(Document, ConcurrencyControl.LAST_WRITE_WINS)
     *
     * @param document The document.
     * @throws CouchbaseLiteException on error
     */
    @Deprecated(
        "Use defaultCollection.delete()",
        ReplaceWith("defaultCollection.delete(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun delete(document: Document)

    /**
     * Deletes a document from the default collection. When used with lastWriteWins concurrency
     * control, the last write operation will win if there is a conflict.
     * When used with FAIL_ON_CONFLICT concurrency control, delete will fail and the method will return false.
     *
     * @param document           The document.
     * @param concurrencyControl The concurrency control.
     * @throws CouchbaseLiteException on error
     */
    @Deprecated(
        "Use defaultCollection.delete()",
        ReplaceWith("defaultCollection.delete(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean

    /**
     * Purges the passed document from the default collection. This is more drastic than delete(Document):
     * it removes all local traces of the document. Purges will NOT be replicated to other databases.
     *
     * @param document the document to be purged.
     */
    @Deprecated(
        "Use defaultCollection.purge()",
        ReplaceWith("defaultCollection.purge(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun purge(document: Document)

    /**
     * Purges the document with the passed id from default collection. This is more drastic than delete(Document),
     * it removes all local traces of the document. Purges will NOT be replicated to other databases.
     *
     * @param id the document ID
     */
    @Deprecated(
        "Use defaultCollection.purge()",
        ReplaceWith("defaultCollection.purge(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun purge(id: String)

    /**
     * Sets an expiration date for a document in the default collection. The document
     * will be purged from the database at the set time.
     *
     * @param id         The ID of the Document
     * @param expiration Nullable expiration timestamp as an Instant date, set timestamp to null
     * to remove expiration date time from doc.
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Deprecated(
        "Use defaultCollection.setDocumentExpiration()",
        ReplaceWith("defaultCollection.setDocumentExpiration(id, expiration)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun setDocumentExpiration(id: String, expiration: Instant?)

    /**
     * Returns the expiration time of the document. If the document has no expiration time set,
     * the method will return null.
     *
     * @param id The ID of the Document
     * @return Date a nullable expiration timestamp of the document or null if time not set.
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Deprecated(
        "Use defaultCollection.getDocumentExpiration()",
        ReplaceWith("defaultCollection.getDocumentExpiration(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun getDocumentExpiration(id: String): Instant?

    /**
     * Adds a change listener for the changes that occur in the database, in the default collection.
     *
     * The changes will be delivered on the main thread for platforms that support it: Android, iOS, and macOS.
     * Callbacks are on an arbitrary thread for the JVM, Linux, and Windows platform.
     *
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     * @throws IllegalStateException if the default collection doesn’t exist.
     *
     * @see ListenerToken.remove
     */
    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(listener)")
    )
    public fun addChangeListener(listener: DatabaseChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the database, in the default collection,
     * with a [CoroutineContext] that will be used to launch coroutines the listener will be called on.
     * Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     * @throws IllegalStateException if the default collection doesn’t exist.
     *
     * @see ListenerToken.remove
     */
    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(context, listener)")
    )
    public fun addChangeListener(context: CoroutineContext, listener: DatabaseChangeSuspendListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur in the database, in the default collection,
     * with a [CoroutineScope] that will be used to launch coroutines the listener will be called on.
     * The listener is removed when the scope is canceled.
     * @throws IllegalStateException if the default collection doesn’t exist.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to post changes.
     */
    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(scope, listener)")
    )
    public fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener)

    /**
     * Adds a change listener for the changes that occur to the specified document, in the default collection.
     *
     * The changes will be delivered on the main thread for platforms that support it: Android, iOS, and macOS.
     * Callbacks are on an arbitrary thread for the JVM, Linux, and Windows platform.
     *
     * @param id document ID
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, listener)")
    )
    public fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken

    /**
     * Adds a change listener for the changes that occur to the specified document, in the default collection,
     * with a [CoroutineContext] that will be used to launch coroutines the listener will be called on.
     * Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param id document ID
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, context, listener)")
    )
    public fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken

    /**
     * Adds a change listener for the changes that occur to the specified document, in the default collection,
     * with a [CoroutineScope] that will be used to launch coroutines the listener will be called on.
     * The listener is removed when the scope is canceled.
     *
     * @param id document ID
     * @param scope coroutine scope in which the listener will run
     * @param listener callback
     */
    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, scope, listener)")
    )
    public fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener)

    /**
     * Removes a change listener added to the default collection.
     *
     * @param token returned by a previous call to [addChangeListener] or [addDocumentChangeListener].
     */
    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    public fun removeChangeListener(token: ListenerToken)

    /**
     * Get a list of the names of indices on the default collection.
     *
     * @return the list of index names
     * @throws CouchbaseLiteException on failure
     */
    @Deprecated(
        "Use defaultCollection.indexes",
        ReplaceWith("defaultCollection.indexes")
    )
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val indexes: List<String>

    /**
     * Add an index to the default collection.
     *
     * @param name  index name
     * @param index index description
     * @throws CouchbaseLiteException on failure
     */
    @Deprecated(
        "Use defaultCollection.createIndex()",
        ReplaceWith("defaultCollection.createIndex(name, index)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, index: Index)

    /**
     * Add an index to the default collection.
     *
     * @param name   index name
     * @param config index configuration
     * @throws CouchbaseLiteException on failure
     */
    @Deprecated(
        "Use defaultCollection.createIndex()",
        ReplaceWith("defaultCollection.createIndex(name, config)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, config: IndexConfiguration)

    /**
     * Delete the named index from the default collection.
     *
     * @param name name of the index to delete
     * @throws CouchbaseLiteException on failure
     */
    @Deprecated(
        "Use defaultCollection.deleteIndex()",
        ReplaceWith("defaultCollection.deleteIndex(name)")
    )
    @Throws(CouchbaseLiteException::class)
    public fun deleteIndex(name: String)

    /**
     * Perform database maintenance.
     */
    @Throws(CouchbaseLiteException::class)
    public fun performMaintenance(type: MaintenanceType): Boolean
}

/**
 * Gets document fragment object by the given document ID.
 *
 * @param key The key.
 */
@Deprecated(
    "Use defaultCollection.get()",
    ReplaceWith("defaultCollection[key]")
)
@Suppress("DEPRECATION")
public operator fun Database.get(key: String): DocumentFragment =
    DocumentFragment(getDocument(key))
