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

import com.couchbase.lite.UnitOfWork
import kotbase.internal.DelegatedClass
import kotbase.ext.toDate
import kotbase.ext.toFile
import kotbase.ext.toKotlinInstant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.datetime.Instant
import java.io.File
import kotlin.coroutines.CoroutineContext
import com.couchbase.lite.Database as CBLDatabase
import com.couchbase.lite.DatabaseConfiguration as CBLDatabaseConfiguration

public actual class Database
internal constructor(actual: CBLDatabase) : DelegatedClass<CBLDatabase>(actual), AutoCloseable {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(CBLDatabase(name))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(CBLDatabase(name, config.actual))

    public actual companion object {

        init {
            internalInit()
        }

        public actual val log: Log by lazy { Log(CBLDatabase.log) }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            CBLDatabase.delete(name, directory?.toFile())
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            CBLDatabase.exists(
                name,
                directory?.let { File(it) }
            )

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            CBLDatabase.copy(
                File(path),
                name,
                config?.actual ?: CBLDatabaseConfiguration()
            )
        }
    }

    public actual val name: String
        get() = actual.name

    public actual val path: String?
        get() = actual.path

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

    @Throws(CouchbaseLiteException::class)
    actual override fun close() {
        actual.close()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        actual.delete()
    }

    @get:Throws(CouchbaseLiteException::class)
    public actual val scopes: Set<Scope>
        get () = actual.scopes.asScopes(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getScope(name: String): Scope? =
        actual.getScope(name)?.asScope(this)

    @get:Throws(CouchbaseLiteException::class)
    public actual val defaultScope: Scope
        get() = Scope(actual.defaultScope, this)

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(name: String): Collection =
        Collection(actual.createCollection(name), this)

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(collectionName: String, scopeName: String?): Collection =
        Collection(actual.createCollection(collectionName, scopeName), this)

    @get:Throws(CouchbaseLiteException::class)
    public actual val collections: Set<Collection>
        get() = actual.collections.asCollections(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(scopeName: String?): Set<Collection> =
        actual.getCollections(scopeName).asCollections(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(name: String): Collection? =
        actual.getCollection(name)?.asCollection(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String, scopeName: String?): Collection? =
        actual.getCollection(collectionName, scopeName)?.asCollection(this)

    @get:Throws(CouchbaseLiteException::class)
    public actual val defaultCollection: Collection by lazy {
        actual.defaultCollection!!.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(name: String) {
        actual.deleteCollection(name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(collectionName: String, scopeName: String?) {
        actual.deleteCollection(collectionName, scopeName)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun <R> inBatch(work: Database.() -> R): R {
        var result: R? = null
        actual.inBatch(UnitOfWork {
            result = this.work()
        })
        @Suppress("UNCHECKED_CAST")
        return result as R
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query =
        DelegatedQuery(actual.createQuery(query))

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.count",
        ReplaceWith("defaultCollection.count")
    )
    public actual val count: Long
        get() = actual.count

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.getDocument()",
        ReplaceWith("defaultCollection.getDocument(id)")
    )
    public actual fun getDocument(id: String): Document? =
        actual.getDocument(id)?.asDocument(defaultCollection)

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        actual.save(document.actual)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean =
        actual.save(document.actual, concurrencyControl)

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document, conflictHandler)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean =
        actual.save(document.actual, conflictHandler.convert(defaultCollection))

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.delete()",
        ReplaceWith("defaultCollection.delete(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        actual.delete(document.actual)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.delete()",
        ReplaceWith("defaultCollection.delete(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean =
        actual.delete(document.actual, concurrencyControl)

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.purge()",
        ReplaceWith("defaultCollection.purge(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        actual.purge(document.actual)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.purge()",
        ReplaceWith("defaultCollection.purge(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        actual.purge(id)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.setDocumentExpiration()",
        ReplaceWith("defaultCollection.setDocumentExpiration(id, expiration)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        actual.setDocumentExpiration(id, expiration?.toDate())
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.getDocumentExpiration()",
        ReplaceWith("defaultCollection.getDocumentExpiration(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? =
        actual.getDocumentExpiration(id)?.toKotlinInstant()

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(listener)")
    )
    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken =
        DelegatedListenerToken(actual.addChangeListener(listener.convert(this)))

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(context, listener)")
    )
    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: DatabaseChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListener(context[CoroutineDispatcher]?.asExecutor(), listener.convert(this, scope))
        return SuspendListenerToken(scope, token)
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(scope, listener)")
    )
    public actual fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener) {
        val token = actual.addChangeListener(
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(this, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, listener)")
    )
    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken =
        DelegatedListenerToken(actual.addDocumentChangeListener(id, listener.convert(defaultCollection)))

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, context, listener)")
    )
    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addDocumentChangeListener(
            id,
            context[CoroutineDispatcher]?.asExecutor(),
            listener.convert(defaultCollection, scope)
        )
        return SuspendListenerToken(scope, token)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, scope, listener)")
    )
    public actual fun addDocumentChangeListener(
        id: String,
        scope: CoroutineScope,
        listener: DocumentChangeSuspendListener
    ) {
        val token = actual.addDocumentChangeListener(
            id,
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(defaultCollection, scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    public actual fun removeChangeListener(token: ListenerToken) {
        token.remove()
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.indexes",
        ReplaceWith("defaultCollection.indexes")
    )
    @get:Throws(CouchbaseLiteException::class)
    public actual val indexes: List<String>
        get() = actual.indexes

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.createIndex()",
        ReplaceWith("defaultCollection.createIndex(name, index)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        actual.createIndex(name, index.actual)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.createIndex()",
        ReplaceWith("defaultCollection.createIndex(name, config)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        actual.createIndex(name, config.actual)
    }

    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection.deleteIndex()",
        ReplaceWith("defaultCollection.deleteIndex(name)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        actual.deleteIndex(name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean =
        actual.performMaintenance(type)
}
