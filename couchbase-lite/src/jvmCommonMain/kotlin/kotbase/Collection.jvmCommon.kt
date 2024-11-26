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

import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotbase.internal.DelegatedClass
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import kotlin.coroutines.CoroutineContext
import com.couchbase.lite.Collection as CBLCollection

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
        get() = actual.fullName

    public actual val count: Long
        get() = actual.count

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocument(id: String): Document? =
        actual.getDocument(id)?.asDocument(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        actual.save(document.actual).also {
            document.collectionInternal = this
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean {
        return actual.save(document.actual, concurrencyControl).also {
            document.collectionInternal = this
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return actual.save(document.actual, conflictHandler.convert(this)).also {
            document.collectionInternal = this
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        actual.delete(document.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean =
        actual.delete(document.actual, concurrencyControl)

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        actual.purge(document.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        actual.purge(id)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        actual.setDocumentExpiration(id, expiration?.toDate())
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? =
        actual.getDocumentExpiration(id)?.toKotlinInstant()

    public actual fun addChangeListener(listener: CollectionChangeListener): ListenerToken =
        DelegatedListenerToken(actual.addChangeListener(listener.convert(this)))

    public actual fun addChangeListener(context: CoroutineContext, listener: CollectionChangeSuspendListener): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListener(context[CoroutineDispatcher]?.asExecutor(), listener.convert(this, scope))
        return SuspendListenerToken(scope, token)
    }

    public actual fun addChangeListener(scope: CoroutineScope, listener: CollectionChangeSuspendListener) {
        val token = actual.addChangeListener(
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken =
        DelegatedListenerToken(actual.addDocumentChangeListener(id, listener.convert(this)))

    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addDocumentChangeListener(
            id,
            context[CoroutineDispatcher]?.asExecutor(),
            listener.convert(this, scope)
        )
        return SuspendListenerToken(scope, token)
    }

    public actual fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener) {
        val token = actual.addDocumentChangeListener(
            id,
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    @get:Throws(CouchbaseLiteException::class)
    public actual val indexes: Set<String>
        get() = actual.indexes

    @Throws(CouchbaseLiteException::class)
    public actual fun getIndex(name: String): QueryIndex? {
        return actual.getIndex(name)?.asQueryIndex(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        actual.createIndex(name, config.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        actual.createIndex(name, index.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        actual.deleteIndex(name)
    }

    actual override fun close() {
        actual.close()
    }

    public actual companion object
}

internal fun CBLCollection.asCollection(database: Database) = Collection(this, database)

internal fun Iterable<CBLCollection>.asCollections(database: Database) = map { Collection(it, database) }.toSet()
