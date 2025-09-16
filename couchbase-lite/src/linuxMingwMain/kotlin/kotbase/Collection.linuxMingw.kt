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

import cnames.structs.CBLCollection
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.fleece.toList
import kotbase.internal.toKotlinInstant
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Collection
internal constructor(
    actual: CPointer<CBLCollection>,
    database: Database
) : AutoCloseable {

    private val memory = object {
        var closeCalled = false
        val actual = actual
        val database = database
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled && !it.database.isClosed) {
            CBLCollection_Release(it.actual)
        }
    }

    internal val actual: CPointer<CBLCollection>
        get() = memory.actual

    public actual val database: Database
        get() = memory.database

    public actual val scope: Scope
        get() = CBLCollection_Scope(actual)!!.asScope(database)

    public actual val name: String
        get() = CBLCollection_Name(actual).toKString()!!

    public actual val fullName: String
        get() = CBLCollection_FullName(actual).toKString()!!

    public actual val count: Long
        get() = database.withLock {
            if (database.isClosed) 0 else CBLCollection_Count(actual).toLong()
        }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocument(id: String): Document? {
        return wrapCBLError { error ->
            memScoped {
                CBLCollection_GetDocument(actual, id.toFLString(this), error)
            }
        }?.asDocument(database)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        document.willSave(database)
        wrapCBLError { error ->
            CBLCollection_SaveDocument(actual, document.actual, error)
        }
        document.database = database
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            document.willSave(database)
            wrapCBLError { error ->
                CBLCollection_SaveDocumentWithConcurrencyControl(
                    actual,
                    document.actual,
                    concurrencyControl.actual,
                    error
                )
            }.also {
                document.database = database
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

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        document.willSave(database)
        val wrapper = ConflictHandlerWrapper(database, conflictHandler)
        this.conflictHandler = StableRef.create(wrapper)
        return try {
            wrapCBLError { error ->
                CBLCollection_SaveDocumentWithConflictHandler(
                    actual,
                    document.actual,
                    nativeConflictHandler(),
                    this.conflictHandler!!.asCPointer(),
                    error
                )
            }.also { success ->
                if (success) {
                    document.database = database
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

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        wrapCBLError { error ->
            CBLCollection_DeleteDocument(actual, document.actual, error)
        }
        document.isDeleted = true
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            wrapCBLError { error ->
                CBLCollection_DeleteDocumentWithConcurrencyControl(
                    actual,
                    document.actual,
                    concurrencyControl.actual,
                    error
                ).also {
                    document.isDeleted = true
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

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        try {
            wrapCBLError { error ->
                CBLCollection_PurgeDocument(actual, document.actual, error)
            }
        } catch (e: CouchbaseLiteException) {
            if (e.code != CBLError.Code.NOT_FOUND || e.domain != CBLError.Domain.CBLITE || document.revisionID == null) {
                throw e
            }
        }
        document.database = null
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        wrapCBLError { error ->
            memScoped {
                CBLCollection_PurgeDocumentByID(actual, id.toFLString(this), error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        wrapCBLError { error ->
            memScoped {
                CBLCollection_SetDocumentExpiration(
                    actual,
                    id.toFLString(this),
                    expiration?.toEpochMilliseconds()?.convert() ?: 0,
                    error
                )
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return wrapCBLError { error ->
            memScoped {
                CBLCollection_GetDocumentExpiration(actual, id.toFLString(this), error).toKotlinInstant()
            }
        }
    }

    public actual fun addChangeListener(listener: CollectionChangeListener): ListenerToken {
        val holder = CollectionChangeDefaultListenerHolder(listener, this)
        return addNativeChangeListener(holder)
    }

    public actual fun addChangeListener(context: CoroutineContext, listener: CollectionChangeSuspendListener): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = CollectionChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        return SuspendListenerToken(scope, token)
    }

    public actual fun addChangeListener(scope: CoroutineScope, listener: CollectionChangeSuspendListener) {
        val holder = CollectionChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    private fun addNativeChangeListener(holder: CollectionChangeListenerHolder) =
        StableRefListenerToken(holder) {
            CBLCollection_AddChangeListener(actual, nativeChangeListener(), it)!!
        }

    private fun nativeChangeListener(): CBLCollectionChangeListener {
        return staticCFunction { ref, collectionChange ->
            with(ref.to<CollectionChangeListenerHolder>()) {
                val change = CollectionChange(collectionChange!!, collection)
                when (this) {
                    is CollectionChangeDefaultListenerHolder -> listener(change)
                    is CollectionChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
            }
        }
    }

    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken {
        val holder = CollectionDocumentChangeDefaultListenerHolder(listener, this)
        return addNativeDocumentChangeListener(id, holder)
    }

    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = CollectionDocumentChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeDocumentChangeListener(id, holder)
        return SuspendListenerToken(scope, token)
    }

    public actual fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener) {
        val holder = CollectionDocumentChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeDocumentChangeListener(id, holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
        }
    }

    private fun addNativeDocumentChangeListener(
        id: String,
        holder: CollectionDocumentChangeListenerHolder
    ) = StableRefListenerToken(holder) {
        memScoped {
            CBLCollection_AddDocumentChangeListener(
                actual,
                id.toFLString(this),
                nativeDocumentChangeListener(),
                it
            )!!
        }
    }

    private fun nativeDocumentChangeListener(): CBLCollectionDocumentChangeListener {
        return staticCFunction { ref, documentChange ->
            with(ref.to<CollectionDocumentChangeListenerHolder>()) {
                val change = DocumentChange(collection, documentChange!!)
                when (this) {
                    is CollectionDocumentChangeDefaultListenerHolder -> listener(change)
                    is CollectionDocumentChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
            }
        }
    }

    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val indexes: Set<String>
        get() {
            return wrapCBLError { error ->
                val names = CBLCollection_GetIndexNames(actual, error)
                @Suppress("UNCHECKED_CAST")
                (names?.toList(null) as List<String>?)?.also {
                    FLMutableArray_Release(names)
                }?.toSet() ?: emptySet()
            }
        }

    @Throws(CouchbaseLiteException::class)
    public actual fun getIndex(name: String): QueryIndex? {
        return wrapCBLError { error ->
            memScoped {
                CBLCollection_GetIndex(actual, name.toFLString(this), error)?.asQueryIndex(this@Collection)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        wrapCBLError { error ->
            memScoped {
                @Suppress("NO_ELSE_IN_WHEN")
                when (config) {
                    is ValueIndexConfiguration -> CBLCollection_CreateValueIndex(
                        actual,
                        name.toFLString(this),
                        config.actual,
                        error
                    )
                    is FullTextIndexConfiguration -> CBLCollection_CreateFullTextIndex(
                        actual,
                        name.toFLString(this),
                        config.actual,
                        error
                    )
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        wrapCBLError { error ->
            memScoped {
                @Suppress("REDUNDANT_ELSE_IN_WHEN", "KotlinRedundantDiagnosticSuppress")
                when (index) {
                    is ValueIndex -> CBLCollection_CreateValueIndex(
                        actual,
                        name.toFLString(this),
                        index.actual,
                        error
                    )
                    is FullTextIndex -> CBLCollection_CreateFullTextIndex(
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

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        wrapCBLError { error ->
            memScoped {
                CBLCollection_DeleteIndex(actual, name.toFLString(this), error)
            }
        }
    }

    actual override fun close() {
        memory.closeCalled = true
        if (!database.isClosed) CBLCollection_Release(actual)
    }

    public override fun toString(): String =
        "$database.$fullName"

    public override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Collection) return false
        // don't use == here! The database must be the exact same instance.
        return database === other.database && scope == other.scope && name == other.name
    }

    public override fun hashCode(): Int =
        arrayOf(scope, name).contentHashCode()

    public actual companion object
}

internal fun CPointer<CBLCollection>.asCollection(database: Database) = Collection(this, database)
