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

import cocoapods.CouchbaseLite.CBLCollection
import kotbase.ext.asDispatchQueue
import kotbase.ext.toKotlinInstantMillis
import kotbase.ext.wrapCBLError
import kotbase.internal.DelegatedClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalObjCRefinement

public actual class Collection
internal constructor(
    actual: CBLCollection,
    public actual val database: Database
) : DelegatedClass<CBLCollection>(actual), AutoCloseable {

    public actual val scope: Scope
        get() = Scope(actual.scope, database)

    public actual val name: String
        get() = actual.name

    public actual val fullName: String
        get() = "${actual.scope.name}.$name"

    public actual val count: Long
        get() = actual.count.toLong()

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocument(id: String): Document? {
        return wrapCBLError { error ->
            actual.documentWithID(id, error)
        }?.asDocument(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        wrapCBLError { error ->
            actual.saveDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            wrapCBLError { error ->
                actual.saveDocument(document.actual, concurrencyControl.actual, error)
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.CONFLICT || e.domain != CBLError.Domain.CBLITE) throw e
            // Java SDK doesn't throw exception on conflict, only returns false
            false
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return wrapCBLError { error ->
            try {
                actual.saveDocument(document.actual, conflictHandler.convert(this), error)
            } catch (e: Exception) {
                if (e !is CouchbaseLiteException) {
                    throw CouchbaseLiteException(
                        "Conflict handler threw an exception",
                        e,
                        CBLError.Domain.CBLITE,
                        CBLError.Code.CONFLICT
                    )
                } else {
                    if (e.code != CBLError.Code.CONFLICT || e.domain != CBLError.Domain.CBLITE) throw e
                    // Java SDK doesn't throw exception on conflict, only returns false
                    false
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        wrapCBLError { error ->
            actual.deleteDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            wrapCBLError { error ->
                actual.deleteDocument(document.actual, concurrencyControl.actual, error)
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.CONFLICT || e.domain != CBLError.Domain.CBLITE) throw e
            // Java SDK doesn't throw exception on conflict, only returns false
            false
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        try {
            wrapCBLError { error ->
                actual.purgeDocument(document.actual, error)
            }
        } catch (e: CouchbaseLiteException) {
            // Java SDK ignores not found error, except for new document
            val isNew = document.revisionID == null
            if (isNew || e.code != CBLError.Code.NOT_FOUND || e.domain != CBLError.Domain.CBLITE) {
                throw e
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        wrapCBLError { error ->
            actual.purgeDocumentWithID(id, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        wrapCBLError { error ->
            actual.setDocumentExpirationWithID(id, expiration?.toNSDate(), error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return wrapCBLError { error ->
            actual.getDocumentExpirationWithID(id, error)
        }?.toKotlinInstantMillis()
    }

    public actual fun addChangeListener(listener: CollectionChangeListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addChangeListener(listener.convert(this))
        )
    }

    public actual fun addChangeListener(context: CoroutineContext, listener: CollectionChangeSuspendListener): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListenerWithQueue(
            context[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addChangeListener(scope: CoroutineScope, listener: CollectionChangeSuspendListener) {
        val token = actual.addChangeListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addDocumentChangeListenerWithID(id, listener.convert(this))
        )
    }

    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addDocumentChangeListenerWithID(
            id,
            context[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener) {
        val token = actual.addDocumentChangeListenerWithID(
            id,
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val indexes: Set<String>
        get() {
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                actual.indexes(error) as List<String>
            }.toSet()
        }

    /**
     * Get a list of the names of indices in the collection.
     *
     * @throws CouchbaseLiteException on failure
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun indexes(): Set<String> = indexes

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        wrapCBLError { error ->
            actual.createIndexWithName(name, config.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        wrapCBLError { error ->
            actual.createIndex(index.actual, name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        wrapCBLError { error ->
            actual.deleteIndexWithName(name, error)
        }
    }

    actual override fun close() {
        // no close() in Objective-C SDK
        // https://github.com/couchbase/couchbase-lite-ios/blob/335339e291a805bf57bbd2c48897d60ddc108ab8/Objective-C/CBLCollection.mm#L82
    }

    public actual companion object
}

internal fun CBLCollection.asCollection(database: Database) = Collection(this, database)

internal fun Iterable<CBLCollection>.asCollections(database: Database) = map { Collection(it, database) }.toSet()
