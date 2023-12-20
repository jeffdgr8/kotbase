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

@OptIn(ExperimentalStdlibApi::class)
public actual class Collection
internal constructor(
    internal val actual: CPointer<CBLCollection>,
    public actual val database: Database
) : AutoCloseable {

    private val memory = object {
        var closeCalled = false
        val actual = this@Collection.actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLCollection_Release(it.actual)
        }
    }

    public actual val scope: Scope
        get() = CBLCollection_Scope(actual)!!.asScope(database)

    public actual val name: String
        get() = CBLCollection_Name(actual).toKString()!!

    public actual val fullName: String
        get() = "${scope.name}.$name"

    public actual val count: Long
        get() = CBLCollection_Count(actual).toLong()

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocument(id: String): Document? {
        return database.mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLCollection_GetDocument(actual, id.toFLString(this), error)
                }
            }?.asDocument(database)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        database.mustBeOpen {
            document.willSave(database)
            wrapCBLError { error ->
                CBLCollection_SaveDocument(actual, document.actual, error)
            }
            document.database = database
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            database.mustBeOpen {
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
        return database.mustBeOpen {
            document.willSave(database)
            val wrapper = ConflictHandlerWrapper(database, conflictHandler)
            this.conflictHandler = StableRef.create(wrapper)
            try {
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
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        database.mustBeOpen {
            wrapCBLError { error ->
                CBLCollection_DeleteDocument(actual, document.actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return database.mustBeOpen {
            try {
                wrapCBLError { error ->
                    CBLCollection_DeleteDocumentWithConcurrencyControl(
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

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        database.mustBeOpen {
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
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        database.mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLCollection_PurgeDocumentByID(actual, id.toFLString(this), error)
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        database.mustBeOpen {
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
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return database.mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLCollection_GetDocumentExpiration(actual, id.toFLString(this), error).toKotlinInstant()
                }
            }
        }
    }

    public actual fun addChangeListener(listener: CollectionChangeListener): ListenerToken {
        return database.mustBeOpen {
            val holder = CollectionChangeDefaultListenerHolder(listener, this)
            addNativeChangeListener(holder)
        }
    }

    public actual fun addChangeListener(context: CoroutineContext, listener: CollectionChangeSuspendListener): ListenerToken {
        return database.mustBeOpen {
            val scope = CoroutineScope(SupervisorJob() + context)
            val holder = CollectionChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeChangeListener(holder)
            SuspendListenerToken(scope, token)
        }
    }

    public actual fun addChangeListener(scope: CoroutineScope, listener: CollectionChangeSuspendListener) {
        database.mustBeOpen {
            val holder = CollectionChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeChangeListener(holder)
            scope.coroutineContext[Job]?.invokeOnCompletion {
                token.remove()
            }
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
        return database.mustBeOpen {
            val holder = CollectionDocumentChangeDefaultListenerHolder(listener, this)
            addNativeDocumentChangeListener(id, holder)
        }
    }

    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        return database.mustBeOpen {
            val scope = CoroutineScope(SupervisorJob() + context)
            val holder = CollectionDocumentChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeDocumentChangeListener(id, holder)
            SuspendListenerToken(scope, token)
        }
    }

    public actual fun addDocumentChangeListener(id: String, scope: CoroutineScope, listener: DocumentChangeSuspendListener) {
        database.mustBeOpen {
            val holder = CollectionDocumentChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeDocumentChangeListener(id, holder)
            scope.coroutineContext[Job]?.invokeOnCompletion {
                token.remove()
            }
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

    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): Set<String> {
        return database.mustBeOpen {
            wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                (CBLCollection_GetIndexNames(actual, error)
                    ?.toList(null) as List<String>?)?.toSet() ?: emptySet()
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        database.mustBeOpen {
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
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        database.mustBeOpen {
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
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        database.mustBeOpen {
            wrapCBLError { error ->
                memScoped {
                    CBLCollection_DeleteIndex(actual, name.toFLString(this), error)
                }
            }
        }
    }

    actual override fun close() {
        memory.closeCalled = true
        CBLCollection_Release(actual)
    }

    public actual companion object
}

internal fun CPointer<CBLCollection>.asCollection(database: Database) = Collection(this, database)
