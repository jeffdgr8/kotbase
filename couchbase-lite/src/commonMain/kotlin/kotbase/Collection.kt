/*
 * Copyright 2023 Jeff Lockhart
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
 * A `Collection` is a container for documents similar to a table in a relational
 * database. Collections are grouped into namespaces called `scope`s and have a
 * unique name within that scope.
 *
 * Every database contains a default collection named "_default"
 * (the constant `Collection.DEFAULT_NAME`) in the default scope
 * (also named "_default", the constant `Scope.DEFAULT_NAME`)
 *
 * While creating a new collection requires the name of the collection
 * and the name of its scope, there are convenience methods that take only
 * the name of the collection and create the collection in the default scope.
 *
 * The naming rules for collections and scopes are as follows:
 *
 *  *  Must be between 1 and 251 characters in length.
 *  *  Can only contain the characters A-Z, a-z, 0-9, and the symbols _, -, and %.
 *  *  Cannot start with _ or %.
 *  *  Both scope and collection names are case-sensitive.
 *
 * `Collection` objects are only valid during the time the database that
 * contains them is open.  An attempt to use a collection that belongs to a closed
 * database will throw an `IllegalStateException`.
 * An application can hold references to multiple instances of a single database.
 * Under these circumstances, it is possible that a collection will be deleted in
 * one instance before an attempt to use it in another.  Such an attempt will
 * also cause an `IllegalStateException` to be thrown.
 *
 * `Collection`s are `AutoCloseable`.  While garbage
 * collection will manage them correctly, developers are strongly encouraged to
 * use them in try-with-resources blocks or to close them explicitly, after use.
 */
@OptIn(ExperimentalStdlibApi::class)
public expect class Collection : AutoCloseable {

    internal val database: Database

    /**
     * Scope
     */
    public val scope: Scope

    /**
     * The collection name
     */
    public val name: String

    /**
     * The number of documents in the collection.
     */
    public val count: Long

    /**
     * Gets an existing Document object with the given ID. If the document with the given ID doesn't
     * exist in the collection, the value returned will be null.
     *
     * @param id the document id
     * @return the Document object or null
     * @throws CouchbaseLiteException if the database is closed, the collection has been deleted, etc.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getDocument(id: String): Document?

    /**
     * Save a document into the collection. The default concurrency control, lastWriteWins,
     * will be used when there is conflict during  save.
     *
     * When saving a document that already belongs to a collection, the collection instance of
     * the document and this collection instance must be the same, otherwise, the InvalidParameter
     * error will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument)

    /**
     * Save a document into the collection with a specified concurrency control. When specifying
     * the failOnConflict concurrency control, and conflict occurred, the save operation will fail with
     * 'false' value returned.
     * When saving a document that already belongs to a collection, the collection instance of the
     * document and this collection instance must be the same, otherwise, the InvalidParameter
     * error will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean

    /**
     * Save a document into the collection with a specified conflict handler. The specified conflict handler
     * will be called if there is conflict during save. If the conflict handler returns 'false', the save
     * operation will be canceled with 'false' value returned.
     *
     * When saving a document that already belongs to a collection, the collection instance of the
     * document and this collection instance must be the same, otherwise, the InvalidParameter error
     * will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean

    /**
     * Delete a document from the collection. The default concurrency control, lastWriteWins, will be used
     * when there is conflict during delete. If the document doesn't exist in the collection, the NotFound
     * error will be thrown.
     *
     * When deleting a document that already belongs to a collection, the collection instance of
     * the document and this collection instance must be the same, otherwise, the InvalidParameter error
     * will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun delete(document: Document)

    /**
     * Delete a document from the collection with a specified concurrency control. When specifying
     * the failOnConflict concurrency control, and conflict occurred, the delete operation will fail with
     * 'false' value returned.
     *
     * When deleting a document, the collection instance of the document and this collection instance
     * must be the same, otherwise, the InvalidParameter error will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean

    /**
     * When purging a document, the collection instance of the document and this collection instance
     * must be the same, otherwise, the InvalidParameter error will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun purge(document: Document)

    /**
     * Purge a document by id from the collection. If the document doesn't exist in the collection,
     * the NotFound error will be thrown.
     */
    @Throws(CouchbaseLiteException::class)
    public fun purge(id: String)

    /**
     * Set an expiration date to the document of the given id. Setting a nil date will clear the expiration.
     */
    @Throws(CouchbaseLiteException::class)
    public fun setDocumentExpiration(id: String, expiration: Instant?)

    /**
     * Get the expiration date set to the document of the given id.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getDocumentExpiration(id: String): Instant?

    /**
     * Add a change listener to listen to change events occurring to any documents in the collection.
     * To remove the listener, call remove() function on the returned listener token.
     *
     * @param listener the observer
     * @return token used to cancel the listener
     *
     * @see ListenerToken.remove
     */
    public fun addChangeListener(listener: CollectionChangeListener): ListenerToken

    /**
     * Add a change listener to listen to change events occurring to any documents in the collection.
     * To remove the listener, call remove() function on the returned listener token.
     *
     * The [CoroutineContext] will be used to launch coroutines the listener will be called on.
     * Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener the observer
     * @return token used to cancel the listener
     *
     * @see ListenerToken.remove
     */
    public fun addChangeListener(context: CoroutineContext, listener: CollectionChangeSuspendListener): ListenerToken

    /**
     * Add a change listener to listen to change events occurring to any documents in the collection.
     *
     * The [CoroutineScope] will be used to launch coroutines the listener will be called on.
     * The listener is removed when the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener the observer
     */
    public fun addChangeListener(scope: CoroutineScope, listener: CollectionChangeSuspendListener)

    /**
     * Add a change listener to listen to change events occurring to a document of the given document id.
     * To remove the listener, call remove() function on the returned listener token.
     *
     * @param id document ID
     * @param listener The listener to post changes.
     *
     * @see ListenerToken.remove
     */
    public fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken

    /**
     * Add a change listener to listen to change events occurring to a document of the given document id.
     *
     * The [CoroutineContext] will be used to launch coroutines the listener will be called on.
     * Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param id document ID
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken

    /**
     * Add a change listener to listen to change events occurring to a document of the given document id.
     *
     * The [CoroutineScope] will be used to launch coroutines the listener will be called on.
     * The listener is removed when the scope is canceled.
     *
     * @param id document ID
     * @param scope coroutine scope in which the listener will run
     * @param listener callback
     */
    public fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener)

    /**
     * Get a list of the names of indices in the collection.
     *
     * @return the list of index names
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun getIndexes(): Set<String>

    /**
     * Add an index to the collection.
     *
     * @param name   index name
     * @param config index configuration
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, config: IndexConfiguration)

    /**
     * Add an index to the collection.
     *
     * @param name  index name
     * @param index index configuration
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createIndex(name: String, index: Index)

    /**
     * Delete the named index from the collection.
     *
     * @param name name of the index to delete
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun deleteIndex(name: String)

    public override fun close()

    public companion object
}

public val Collection.Companion.DEFAULT_NAME: String
    get() = "_default"

internal val Collection.fullName: String
    get() = "${scope.name}.$name"
