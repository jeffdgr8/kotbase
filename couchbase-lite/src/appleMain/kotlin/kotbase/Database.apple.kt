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

import cocoapods.CouchbaseLite.*
import kotbase.internal.DelegatedClass
import kotbase.ext.asDispatchQueue
import kotbase.ext.dispatcher
import kotbase.ext.toKotlinInstantMillis
import kotbase.ext.wrapCBLError
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Instant
import kotlinx.datetime.toNSDate
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalObjCRefinement

public actual class Database
internal constructor(actual: CBLDatabase) : DelegatedClass<CBLDatabase>(actual), AutoCloseable {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(
        wrapCBLError { error ->
            require(name.isNotEmpty()) { "db name must not be empty" }
            CBLDatabase(name, error)
        }
    )

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(
        wrapCBLError { error ->
            CBLDatabase(name, config.actual, error)
        }
    )

    public actual companion object {

        @Suppress("DEPRECATION")
        @Deprecated("Use LogSinks.file")
        public actual val log: Log by lazy {
            Log(CBLDatabase.log())
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            // Java SDK throws not found error
            if (!exists(name, directory ?: DatabaseConfiguration(null).directory)) {
                throw CouchbaseLiteException(
                    "Database not found for delete",
                    CBLError.Domain.CBLITE,
                    CBLError.Code.NOT_FOUND
                )
            }
            wrapCBLError { error ->
                CBLDatabase.deleteDatabase(name, directory, error)
            }
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            CBLDatabase.databaseExists(name, directory)

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            wrapCBLError { error ->
                CBLDatabase.copyFromPath(path, name, config?.actual, error)
            }
        }
    }

    public actual val name: String
        get() = actual.name

    public actual val path: String?
        get() = actual.path

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

    actual override fun close() {
        withLock {
            wrapCBLError { error ->
                actual.close(error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        mustBeOpen {
            wrapCBLError { error ->
                actual.delete(error)
            }
        }
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val scopes: Set<Scope>
        get() {
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                actual.scopes(error) as List<CBLScope>
            }.asScopes(this)
        }

    /**
     * Get scope names that have at least one collection.
     * Note: the default scope is exceptional as it will always be listed even though there are no collections
     * under it.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun scopes(): Set<Scope> = scopes

    @Throws(CouchbaseLiteException::class)
    public actual fun getScope(name: String): Scope? {
        return wrapCBLError { error ->
            actual.scopeWithName(name, error)
        }?.asScope(this)
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val defaultScope: Scope
        get() {
            return wrapCBLError { error ->
                actual.defaultScope(error)
            }!!.asScope(this)
        }

    /**
     * Get the default scope.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun defaultScope(): Scope = defaultScope

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(name: String): Collection {
        return wrapCBLError { error ->
            actual.createCollectionWithName(name, null, error)
        }!!.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(collectionName: String, scopeName: String?): Collection {
        return wrapCBLError { error ->
            actual.createCollectionWithName(collectionName, scopeName, error)
        }!!.asCollection(this)
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val collections: Set<Collection>
        get() {
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                actual.collections(null, error) as List<CBLCollection>
            }.asCollections(this)
        }

    /**
     * Get all collections in the default scope.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun collections(): Set<Collection> = collections

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(scopeName: String?): Set<Collection> {
        return wrapCBLError { error ->
            @Suppress("UNCHECKED_CAST")
            actual.collections(scopeName, error) as List<CBLCollection>
        }.asCollections(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(name: String): Collection? {
        return wrapCBLError { error ->
            actual.collectionWithName(name, null, error)
        }?.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String, scopeName: String?): Collection? {
        return wrapCBLError { error ->
            actual.collectionWithName(collectionName, scopeName, error)
        }?.asCollection(this)
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val defaultCollection: Collection by lazy {
        wrapCBLError { error ->
            actual.defaultCollection(error)
        }!!.asCollection(this)
    }

    /**
     * Get the default collection.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun defaultCollection(): Collection = defaultCollection

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(name: String) {
        wrapCBLError { error ->
            actual.deleteCollectionWithName(name, null, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(collectionName: String, scopeName: String?) {
        wrapCBLError { error ->
            actual.deleteCollectionWithName(collectionName, scopeName, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun <R> inBatch(work: Database.() -> R): R {
        return mustBeOpen {
            @Suppress("UNCHECKED_CAST")
            wrapCBLError { error ->
                var result: R? = null
                actual.inBatch(error) {
                    result = this@Database.work()
                }
                result
            } as R
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query {
        val actualQuery = mustBeOpen {
            wrapCBLError { error ->
                actual.createQuery(query, error)
            }
        }
        return DelegatedQuery(actualQuery!!)
    }

    @Deprecated(
        "Use defaultCollection.count",
        ReplaceWith("defaultCollection.count")
    )
    public actual val count: Long
        get() = actual.count.toLong()

    @Deprecated(
        "Use defaultCollection.getDocument()",
        ReplaceWith("defaultCollection.getDocument(id)")
    )
    public actual fun getDocument(id: String): Document? {
        return mustBeOpen {
            actual.documentWithID(id)?.asDocument(defaultCollection)
        }
    }

    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.saveDocument(document.actual, error)
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        return try {
            mustBeOpen {
                wrapCBLError { error ->
                    actual.saveDocument(document.actual, concurrencyControl.actual, error)
                }
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.CONFLICT || e.domain != CBLError.Domain.CBLITE) throw e
            // Java SDK doesn't throw exception on conflict, only returns false
            false
        }
    }

    @Deprecated(
        "Use defaultCollection.save()",
        ReplaceWith("defaultCollection.save(document, conflictHandler)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return mustBeOpen {
            wrapCBLError { error ->
                try {
                    actual.saveDocument(document.actual, conflictHandler.convert(defaultCollection), error)
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
    }

    @Deprecated(
        "Use defaultCollection.delete()",
        ReplaceWith("defaultCollection.delete(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.deleteDocument(document.actual, error)
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.delete()",
        ReplaceWith("defaultCollection.delete(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            mustBeOpen {
                wrapCBLError { error ->
                    actual.deleteDocument(document.actual, concurrencyControl.actual, error)
                }
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.CONFLICT || e.domain != CBLError.Domain.CBLITE) throw e
            // Java SDK doesn't throw exception on conflict, only returns false
            false
        }
    }

    @Deprecated(
        "Use defaultCollection.purge()",
        ReplaceWith("defaultCollection.purge(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        try {
            mustBeOpen {
                wrapCBLError { error ->
                    actual.purgeDocument(document.actual, error)
                }
            }
        } catch (e: CouchbaseLiteException) {
            // Java SDK ignores not found error, except for new document
            val isNew = document.revisionID == null
            if (isNew || e.code != CBLError.Code.NOT_FOUND || e.domain != CBLError.Domain.CBLITE) {
                throw e
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.purge()",
        ReplaceWith("defaultCollection.purge(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.purgeDocumentWithID(id, error)
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.setDocumentExpiration()",
        ReplaceWith("defaultCollection.setDocumentExpiration(id, expiration)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.setDocumentExpirationWithID(id, expiration?.toNSDate(), error)
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.getDocumentExpiration()",
        ReplaceWith("defaultCollection.getDocumentExpiration(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return mustBeOpen {
            actual.getDocumentExpirationWithID(id)?.toKotlinInstantMillis()
        }
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(listener)")
    )
    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken {
        return DelegatedListenerToken(
            mustBeOpen {
                actual.addChangeListener(listener.convert(this))
            }
        )
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(context, listener)")
    )
    public actual fun addChangeListener(context: CoroutineContext, listener: DatabaseChangeSuspendListener): ListenerToken {
        return mustBeOpen {
            val scope = CoroutineScope(SupervisorJob() + context)
            val token = actual.addChangeListenerWithQueue(
                context.dispatcher?.asDispatchQueue(),
                listener.convert(this, scope)
            )
            SuspendListenerToken(scope, token)
        }
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use defaultCollection.addChangeListener()",
        ReplaceWith("defaultCollection.addChangeListener(scope, listener)")
    )
    public actual fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener) {
        mustBeOpen {
            val token = actual.addChangeListenerWithQueue(
                scope.coroutineContext.dispatcher?.asDispatchQueue(),
                listener.convert(this, scope)
            )
            scope.coroutineContext[Job]?.invokeOnCompletion {
                token.remove()
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, listener)")
    )
    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken {
        return DelegatedListenerToken(
            mustBeOpen {
                actual.addDocumentChangeListenerWithID(id, listener.convert(defaultCollection))
            }
        )
    }

    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, context, listener)")
    )
    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        return mustBeOpen {
            val scope = CoroutineScope(SupervisorJob() + context)
            val token = actual.addDocumentChangeListenerWithID(
                id,
                context.dispatcher?.asDispatchQueue(),
                listener.convert(defaultCollection, scope)
            )
            SuspendListenerToken(scope, token)
        }
    }

    @Deprecated(
        "Use defaultCollection.addDocumentChangeListener()",
        ReplaceWith("defaultCollection.addDocumentChangeListener(id, scope, listener)")
    )
    public actual fun addDocumentChangeListener(
        id: String,
        scope: CoroutineScope,
        listener: DocumentChangeSuspendListener
    ) {
        mustBeOpen {
            val token = actual.addDocumentChangeListenerWithID(
                id,
                scope.coroutineContext.dispatcher?.asDispatchQueue(),
                listener.convert(defaultCollection, scope)
            )
            scope.coroutineContext[Job]?.invokeOnCompletion {
                actual.removeChangeListenerWithToken(token)
            }
        }
    }

    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    public actual fun removeChangeListener(token: ListenerToken) {
        token.remove()
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Deprecated(
        "Use defaultCollection.indexes",
        ReplaceWith("defaultCollection.indexes")
    )
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val indexes: List<String>
        get() {
            return mustBeOpen {
                @Suppress("UNCHECKED_CAST")
                actual.indexes as List<String>
            }
        }

    /**
     * Get a list of the names of indices on the default collection.
     *
     * @throws CouchbaseLiteException on failure
     */
    // For Objective-C/Swift throws
    @Suppress("DEPRECATION")
    @Deprecated(
        "Use defaultCollection().indexes()",
        ReplaceWith("defaultCollection().indexes()")
    )
    @Throws(CouchbaseLiteException::class)
    public fun indexes(): List<String> = indexes

    @Deprecated(
        "Use defaultCollection.createIndex()",
        ReplaceWith("defaultCollection.createIndex(name, index)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.createIndex(index.actual, name, error)
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.createIndex()",
        ReplaceWith("defaultCollection.createIndex(name, config)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.createIndexWithConfig(config.actual, name, error)
            }
        }
    }

    @Deprecated(
        "Use defaultCollection.deleteIndex()",
        ReplaceWith("defaultCollection.deleteIndex(name)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.deleteIndexForName(name, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return mustBeOpen {
            wrapCBLError { error ->
                actual.performMaintenance(type.actual, error)
            }
        }
    }

    internal fun mustBeOpen() {
        mustBeOpen { }
    }

    private val lock = reentrantLock()

    private inline fun <R> withLock(action: () -> R): R =
        lock.withLock(action)

    private fun <R> mustBeOpen(action: () -> R): R {
        return withLock {
            if (actual.isClosed()) {
                throw CouchbaseLiteError("Attempt to perform an operation on a closed database or a deleted collection.")
            }
            action()
        }
    }
}
