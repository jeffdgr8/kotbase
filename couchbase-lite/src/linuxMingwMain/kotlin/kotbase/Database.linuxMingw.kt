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

import cnames.structs.CBLDatabase
import cnames.structs.CBLQuery
import kotbase.internal.fleece.iterator
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.fleece.toList
import kotbase.internal.toExceptionNotNull
import kotbase.internal.toKotlinInstant
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotbase.util.toList
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalStdlibApi::class)
public actual class Database
private constructor(
    internal val actual: CPointer<CBLDatabase>,
    private val _config: DatabaseConfiguration
) : AutoCloseable {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLDatabase_Release(it)
    }

    internal var isClosed = false

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(name, DatabaseConfiguration(null))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(
        try {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_Open(name.toFLString(this), config.actual, error)
                }
            }!!
        } catch (e: CouchbaseLiteException) {
            if (e.code == CBLError.Code.INVALID_PARAMETER && e.domain == CBLError.Domain.CBLITE) {
                throw IllegalArgumentException("Invalid parameter", e)
            } else {
                throw e
            }
        },
        config.also {
            it.readonly = true
        }
    )

    public actual companion object {

        public actual val log: Log by lazy {
            Log()
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
                memScoped {
                    CBL_DeleteDatabase(name.toFLString(this), directory.toFLString(this), error)
                }
            }
        }

        public actual fun exists(name: String, directory: String?): Boolean {
            return memScoped {
                CBL_DatabaseExists(name.toFLString(this), directory.toFLString(this))
            }
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            wrapCBLError { error ->
                memScoped {
                    CBL_CopyDatabase(
                        path.toFLString(this),
                        name.toFLString(this),
                        config?.actual,
                        error
                    )
                }
            }
        }
    }

    public actual val name: String
        get() = CBLDatabase_Name(actual).toKString()!!

    public actual val path: String?
        get() = if (isClosed) null else CBLDatabase_Path(actual).toKString()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(_config)

    actual override fun close() {
        withLock {
            wrapCBLError { error ->
                CBLDatabase_Close(actual, error)
            }
            isClosed = true
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_Delete(actual, error)
            }
            isClosed = true
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getScopes(): Set<Scope> {
        val names = wrapCBLError { error ->
            CBLDatabase_ScopeNames(actual, error)
        }
        return buildSet {
            memScoped {
                names?.iterator(this)?.forEach {
                    wrapCBLError { error ->
                        CBLDatabase_Scope(actual, FLValue_AsString(it), error)
                    }?.asScope(this@Database)?.let(::add)
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getScope(name: String): Scope? {
        return wrapCBLError { error ->
            memScoped {
                CBLDatabase_Scope(actual, name.toFLString(this), error)
            }
        }?.asScope(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDefaultScope(): Scope {
        return wrapCBLError { error ->
            CBLDatabase_DefaultScope(actual, error)
        }!!.asScope(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(name: String): Collection =
        createCollection(name, getDefaultScope().name)

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(collectionName: String, scopeName: String?): Collection {
        return wrapCBLError { error ->
            memScoped {
                CBLDatabase_CreateCollection(
                    actual,
                    collectionName.toFLString(this),
                    scopeName.toFLString(this),
                    error
                )
            }
        }!!.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(): Set<Collection> =
        getCollections(getDefaultScope().name)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(scopeName: String?): Set<Collection> {
        return memScoped {
            val scope = scopeName.toFLString(this)
            val names = wrapCBLError { error ->
                CBLDatabase_CollectionNames(actual, scope, error)
            }
            buildSet {
                memScoped {
                    names?.iterator(this)?.forEach {
                        wrapCBLError { error ->
                            CBLDatabase_Collection(actual, FLValue_AsString(it), scope, error)
                        }?.asCollection(this@Database)?.let(::add)
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(name: String): Collection? =
        getCollection(name, getDefaultScope().name)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String, scopeName: String?): Collection? {
        return wrapCBLError { error ->
            memScoped {
                CBLDatabase_Collection(
                    actual,
                    collectionName.toFLString(this),
                    scopeName.toFLString(this),
                    error
                )
            }
        }?.asCollection(this)
    }

    private val _defaultCollection: Collection? by lazy {
        wrapCBLError { error ->
            CBLDatabase_DefaultCollection(actual, error)
        }?.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDefaultCollection(): Collection? =
        _defaultCollection

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(name: String) {
        deleteCollection(name, getDefaultScope().name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(collectionName: String, scopeName: String?) {
        wrapCBLError { error ->
            memScoped {
                CBLDatabase_DeleteCollection(
                    actual,
                    name.toFLString(this),
                    scopeName.toFLString(this),
                    error
                )
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun <R> inBatch(work: Database.() -> R): R {
        return mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_BeginTransaction(actual, error)
            }

            val result: R
            var commit = false
            try {
                result = this@Database.work()
                commit = true
            } finally {
                wrapCBLError { error ->
                    CBLDatabase_EndTransaction(actual, commit, error)
                }
            }
            result
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query =
        DelegatedQuery(createQuery(kCBLN1QLLanguage, query), this)

    internal fun createQuery(language: CBLQueryLanguage, queryString: String): CPointer<CBLQuery> {
        return memScoped {
            val errorPos = alloc<IntVar>()
            wrapCBLError({
                toExceptionNotNull(mapOf("position" to errorPos.value))
            }) { error ->
                mustBeOpen {
                    memScoped {
                        CBLDatabase_CreateQuery(
                            actual,
                            language,
                            queryString.toFLString(this),
                            errorPos.ptr,
                            error
                        )!!
                    }
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().count",
        ReplaceWith("getDefaultCollection().count")
    )
    public actual val count: Long
        get() = CBLDatabase_Count(actual).toLong()

    @Deprecated(
        "Use getDefaultCollection().getDocument()",
        ReplaceWith("getDefaultCollection().getDocument(id)")
    )
    public actual fun getDocument(id: String): Document? {
        return mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_GetDocument(actual, id.toFLString(this), error)
                        ?.asDocument(this@Database)
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().save()",
        ReplaceWith("getDefaultCollection().save(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        mustBeOpen {
            document.willSave(this)
            wrapCBLError { error ->
                CBLDatabase_SaveDocument(actual, document.actual, error)
            }
            document.database = this
        }
    }

    @Deprecated(
        "Use getDefaultCollection().save()",
        ReplaceWith("getDefaultCollection().save(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        return try {
            mustBeOpen {
                document.willSave(this)
                wrapCBLError { error ->
                    CBLDatabase_SaveDocumentWithConcurrencyControl(
                        actual,
                        document.actual,
                        concurrencyControl.actual,
                        error
                    )
                }.also {
                    document.database = this
                }
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code == CBLError.Code.CONFLICT && e.domain == CBLError.Domain.CBLITE) {
                // Java SDK doesn't throw exception on conflict, only returns false
                false
            } else {
                throw e
            }
        }
    }

    private var conflictHandler: StableRef<ConflictHandlerWrapper>? = null

    @Deprecated(
        "Use getDefaultCollection().save()",
        ReplaceWith("getDefaultCollection().save(document, conflictHandler)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return mustBeOpen {
            document.willSave(this)
            val wrapper = ConflictHandlerWrapper(this, conflictHandler)
            this.conflictHandler = StableRef.create(wrapper)
            try {
                wrapCBLError { error ->
                    CBLDatabase_SaveDocumentWithConflictHandler(
                        actual,
                        document.actual,
                        nativeConflictHandler(),
                        this.conflictHandler!!.asCPointer(),
                        error
                    )
                }.also { success ->
                    if (success) {
                        document.database = this
                    }
                }
            } catch (e: Exception) {
                if (wrapper.exception != null) {
                    throw CouchbaseLiteException(
                        "Conflict handler threw an exception",
                        wrapper.exception!!,
                        CBLError.Domain.CBLITE,
                        CBLError.Code.CONFLICT
                    )
                } else {
                    throw e
                }
            } finally {
                this.conflictHandler?.dispose()
                this.conflictHandler = null
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().delete()",
        ReplaceWith("getDefaultCollection().delete(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_DeleteDocument(actual, document.actual, error)
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().delete()",
        ReplaceWith("getDefaultCollection().delete(document, concurrencyControl)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return mustBeOpen {
            try {
                wrapCBLError { error ->
                    CBLDatabase_DeleteDocumentWithConcurrencyControl(
                        actual,
                        document.actual,
                        concurrencyControl.actual,
                        error
                    ).also {
                        document.database = null
                    }
                }
            } catch (e: CouchbaseLiteException) {
                if (e.code == CBLError.Code.CONFLICT && e.domain == CBLError.Domain.CBLITE) {
                    // Java SDK doesn't throw exception on conflict, only returns false
                    false
                } else {
                    throw e
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().purge()",
        ReplaceWith("getDefaultCollection().purge(document)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        mustBeOpen {
            try {
                wrapCBLError { error ->
                    CBLDatabase_PurgeDocument(actual, document.actual, error)
                }
            } catch (e: CouchbaseLiteException) {
                if (e.code != CBLError.Code.NOT_FOUND || e.domain != CBLError.Domain.CBLITE || document.revisionID == null) {
                    throw e
                }
            }
            document.database = null
        }
    }

    @Deprecated(
        "Use getDefaultCollection().purge()",
        ReplaceWith("getDefaultCollection().purge(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_PurgeDocumentByID(actual, id.toFLString(this), error)
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().setDocumentExpiration()",
        ReplaceWith("getDefaultCollection().setDocumentExpiration(id, expiration)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_SetDocumentExpiration(
                        actual,
                        id.toFLString(this),
                        expiration?.toEpochMilliseconds()?.convert() ?: 0,
                        error
                    )
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().getDocumentExpiration()",
        ReplaceWith("getDefaultCollection().getDocumentExpiration(id)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_GetDocumentExpiration(actual, id.toFLString(this), error).toKotlinInstant()
                }
            }
        }
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use getDefaultCollection().addChangeListener()",
        ReplaceWith("getDefaultCollection().addChangeListener(listener)")
    )
    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken {
        return mustBeOpen {
            val holder = DatabaseChangeDefaultListenerHolder(listener, this)
            addNativeChangeListener(holder)
        }
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use getDefaultCollection().addChangeListener()",
        ReplaceWith("getDefaultCollection().addChangeListener(context, listener)")
    )
    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: DatabaseChangeSuspendListener
    ): ListenerToken {
        return mustBeOpen {
            val scope = CoroutineScope(SupervisorJob() + context)
            val holder = DatabaseChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeChangeListener(holder)
            SuspendListenerToken(scope, token)
        }
    }

    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
    @Deprecated(
        "Use getDefaultCollection().addChangeListener()",
        ReplaceWith("getDefaultCollection().addChangeListener(scope, listener)")
    )
    public actual fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener) {
        mustBeOpen {
            val holder = DatabaseChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeChangeListener(holder)
            scope.coroutineContext[Job]?.invokeOnCompletion {
                token.remove()
            }
        }
    }

    private fun addNativeChangeListener(holder: DatabaseChangeListenerHolder) =
        StableRefListenerToken(holder) {
            CBLDatabase_AddChangeListener(actual, nativeChangeListener(), it)!!
        }

    @Suppress("DEPRECATION")
    private fun nativeChangeListener(): CBLDatabaseChangeListener {
        return staticCFunction { ref, _, numDocs, docIds ->
            val documentIds = docIds!!.toList(numDocs) { it.pointed.toKString()!! }
            with(ref.to<DatabaseChangeListenerHolder>()) {
                val change = DatabaseChange(database, documentIds)
                when (this) {
                    is DatabaseChangeDefaultListenerHolder -> listener(change)
                    is DatabaseChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().addDocumentChangeListener()",
        ReplaceWith("getDefaultCollection().addDocumentChangeListener(id, listener)")
    )
    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken {
        return mustBeOpen {
            val holder = DocumentChangeDefaultListenerHolder(listener, this)
            addNativeDocumentChangeListener(id, holder)
        }
    }

    @Deprecated(
        "Use getDefaultCollection().addDocumentChangeListener()",
        ReplaceWith("getDefaultCollection().addDocumentChangeListener(id, context, listener)")
    )
    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        return mustBeOpen {
            val scope = CoroutineScope(SupervisorJob() + context)
            val holder = DocumentChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeDocumentChangeListener(id, holder)
            SuspendListenerToken(scope, token)
        }
    }

    @Deprecated(
        "Use getDefaultCollection().addDocumentChangeListener()",
        ReplaceWith("getDefaultCollection().addDocumentChangeListener(id, scope, listener)")
    )
    public actual fun addDocumentChangeListener(
        id: String,
        scope: CoroutineScope,
        listener: DocumentChangeSuspendListener
    ) {
        mustBeOpen {
            val holder = DocumentChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeDocumentChangeListener(id, holder)
            scope.coroutineContext[Job]?.invokeOnCompletion {
                token.remove()
            }
        }
    }

    private fun addNativeDocumentChangeListener(
        id: String,
        holder: DocumentChangeListenerHolder
    ) = StableRefListenerToken(holder) {
        memScoped {
            CBLDatabase_AddDocumentChangeListener(
                actual,
                id.toFLString(this),
                nativeDocumentChangeListener(),
                it
            )!!
        }
    }

    private fun nativeDocumentChangeListener(): CBLDocumentChangeListener {
        return staticCFunction { ref, _, docId ->
            with(ref.to<DocumentChangeListenerHolder>()) {
                val change = DocumentChange(database.getDefaultCollectionNotNull(), docId.toKString()!!)
                when (this) {
                    is DocumentChangeDefaultListenerHolder -> listener(change)
                    is DocumentChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
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

    @Deprecated(
        "Use getDefaultCollection().indexes",
        ReplaceWith("getDefaultCollection().indexes")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> {
        return mustBeOpen {
            @Suppress("UNCHECKED_CAST")
            CBLDatabase_GetIndexNames(actual)
                ?.toList(null) as List<String>? ?: emptyList()
        }
    }

    @Deprecated(
        "Use getDefaultCollection().createIndex()",
        ReplaceWith("getDefaultCollection().createIndex(name, index)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    @Suppress("REDUNDANT_ELSE_IN_WHEN", "KotlinRedundantDiagnosticSuppress")
                    when (index) {
                        is ValueIndex -> CBLDatabase_CreateValueIndex(
                            actual,
                            name.toFLString(this),
                            index.actual,
                            error
                        )
                        is FullTextIndex -> CBLDatabase_CreateFullTextIndex(
                            actual,
                            name.toFLString(this),
                            index.actual,
                            error
                        )
                        else -> error("Unhandled Index type ${index::class}")
                    }
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().createIndex()",
        ReplaceWith("getDefaultCollection().createIndex(name, config)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    @Suppress("NO_ELSE_IN_WHEN")
                    when (config) {
                        is ValueIndexConfiguration -> CBLDatabase_CreateValueIndex(
                            actual,
                            name.toFLString(this),
                            config.actual,
                            error
                        )
                        is FullTextIndexConfiguration -> CBLDatabase_CreateFullTextIndex(
                            actual,
                            name.toFLString(this),
                            config.actual,
                            error
                        )
                    }
                }
            }
        }
    }

    @Deprecated(
        "Use getDefaultCollection().deleteIndex()",
        ReplaceWith("getDefaultCollection().deleteIndex(name)")
    )
    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_DeleteIndex(actual, name.toFLString(this), error)
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_PerformMaintenance(actual, type.actual, error)
            }
        }
    }

    internal fun mustBeOpen() {
        mustBeOpen { }
    }

    private val lock = SynchronizedObject()

    internal inline fun <R> withLock(crossinline action: () -> R): R {
        return lock.withLock {
            action()
        }
    }

    internal fun <R> mustBeOpen(action: () -> R): R {
        return withLock {
            if (isClosed) {
                throw IllegalStateException("Attempt to perform an operation on a closed database or a deleted collection.")
            }
            action()
        }
    }
}
