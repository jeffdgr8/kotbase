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

public actual class Database
internal constructor(
    internal val actual: CPointer<CBLDatabase>,
    private val _config: DatabaseConfiguration
) {

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
                CBLDatabase_Open(name.toFLString(), config.actual, error)
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
                CBL_DeleteDatabase(name.toFLString(), directory.toFLString(), error)
            }
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            CBL_DatabaseExists(name.toFLString(), directory.toFLString())

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            wrapCBLError { error ->
                CBL_CopyDatabase(path.toFLString(), name.toFLString(), config?.actual, error)
            }
        }
    }

    public actual val name: String
        get() = CBLDatabase_Name(actual).toKString()!!

    public actual val path: String?
        get() = if (isClosed) null else CBLDatabase_Path(actual).toKString()

    public actual val count: Long
        get() = CBLDatabase_Count(actual).toLong()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(_config)

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

    private class ConflictHandlerWrapper(
        val db: Database,
        val handler: ConflictHandler,
        var exception: Exception? = null
    )

    private var conflictHandler: StableRef<ConflictHandlerWrapper>? = null

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

    private fun nativeConflictHandler(): CBLConflictHandler {
        return staticCFunction { ref, document, oldDocument ->
            with(ref.to<ConflictHandlerWrapper>()) {
                try {
                    handler(
                        MutableDocument(document!!, db),
                        oldDocument?.asDocument(db)
                    )
                } catch (e: Exception) {
                    // save a reference, as Linux will propagate
                    // the error as an "unknown C++ exception"
                    exception = e
                    throw e
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_DeleteDocument(actual, document.actual, error)
            }
        }
    }

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

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        wrapCBLError { error ->
            mustBeOpen {
                CBLDatabase_PurgeDocumentByID(actual, id.toFLString(), error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        wrapCBLError { error ->
            mustBeOpen {
                CBLDatabase_SetDocumentExpiration(
                    actual,
                    id.toFLString(),
                    expiration?.toEpochMilliseconds()?.convert() ?: 0,
                    error
                )
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return wrapCBLError { error ->
            mustBeOpen {
                CBLDatabase_GetDocumentExpiration(actual, id.toFLString(), error).toKotlinInstant()
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

    private val changeListeners = mutableListOf<StableRef<DatabaseChangeListenerHolder>?>()

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken {
        return mustBeOpen {
            val holder = DatabaseChangeDefaultListenerHolder(listener, this)
            addNativeChangeListener(holder)
        }
    }

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

    public actual fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener) {
        mustBeOpen {
            val holder = DatabaseChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeChangeListener(holder)
            scope.coroutineContext[Job]?.invokeOnCompletion {
                removeChangeListener(token)
            }
        }
    }

    private fun addNativeChangeListener(holder: DatabaseChangeListenerHolder): DelegatedListenerToken {
        val (index, stableRef) = addListener(changeListeners, holder)
        return DelegatedListenerToken(
            CBLDatabase_AddChangeListener(
                actual,
                nativeChangeListener(),
                stableRef
            )!!,
            ListenerTokenType.DATABASE,
            index
        )
    }

    private fun nativeChangeListener(): CBLDatabaseChangeListener {
        return staticCFunction { ref, _, numDocs, docIds ->
            val documentIds = docIds!!.toList(numDocs.toInt()) { it.pointed.toKString()!! }
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

    public actual fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            removeChangeListener(token.token)
            token.scope.cancel()
        } else {
            removeChangeListener(token as DelegatedListenerToken)
        }
    }

    private fun removeChangeListener(token: DelegatedListenerToken) {
        val ref = when (token.type) {
            ListenerTokenType.DATABASE -> changeListeners.getOrNull(token.index)
            ListenerTokenType.DOCUMENT -> documentChangeListeners.getOrNull(token.index)
            else -> error("${token.type} change listener can't be removed from Database instance")
        }
        if (ref != null) {
            CBLListener_Remove(token.actual)
            when (token.type) {
                ListenerTokenType.DATABASE -> removeListener(changeListeners, token.index)
                ListenerTokenType.DOCUMENT -> removeListener(documentChangeListeners, token.index)
                else -> error("${token.type} change listener can't be removed from Database instance")
            }
        }
    }

    private val documentChangeListeners = mutableListOf<StableRef<DocumentChangeListenerHolder>?>()

    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken {
        return mustBeOpen {
            val holder = DocumentChangeDefaultListenerHolder(listener, this)
            addNativeDocumentChangeListener(id, holder)
        }
    }

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

    public actual fun addDocumentChangeListener(
        id: String,
        scope: CoroutineScope,
        listener: DocumentChangeSuspendListener
    ) {
        mustBeOpen {
            val holder = DocumentChangeSuspendListenerHolder(listener, this, scope)
            val token = addNativeDocumentChangeListener(id, holder)
            scope.coroutineContext[Job]?.invokeOnCompletion {
                removeChangeListener(token)
            }
        }
    }

    private fun addNativeDocumentChangeListener(
        id: String,
        holder: DocumentChangeListenerHolder
    ): DelegatedListenerToken {
        val (index, stableRef) = addListener(documentChangeListeners, holder)
        return DelegatedListenerToken(
            CBLDatabase_AddDocumentChangeListener(
                actual,
                id.toFLString(),
                nativeDocumentChangeListener(),
                stableRef
            )!!,
            ListenerTokenType.DOCUMENT,
            index
        )
    }

    private fun nativeDocumentChangeListener(): CBLDocumentChangeListener {
        return staticCFunction { ref, _, docId ->
            with(ref.to<DocumentChangeListenerHolder>()) {
                val change = DocumentChange(database, docId.toKString()!!)
                when (this) {
                    is DocumentChangeDefaultListenerHolder -> listener(change)
                    is DocumentChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun close() {
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
    public actual fun createQuery(query: String): Query =
        DelegatedQuery(createQuery(kCBLN1QLLanguage, query), this)

    internal fun createQuery(language: CBLQueryLanguage, queryString: String): CPointer<CBLQuery> {
        return memScoped {
            val errorPos = alloc<IntVar>()
            wrapCBLError({
                toExceptionNotNull(mapOf("position" to errorPos.value))
            }) { error ->
                mustBeOpen {
                    CBLDatabase_CreateQuery(
                        actual,
                        language,
                        queryString.toFLString(),
                        errorPos.ptr,
                        error
                    )!!
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> {
        return mustBeOpen {
            CBLDatabase_GetIndexNames(actual)
                ?.toList(null) as List<String>? ?: emptyList()
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        wrapCBLError { error ->
            mustBeOpen {
                memScoped {
                    @Suppress("REDUNDANT_ELSE_IN_WHEN", "KotlinRedundantDiagnosticSuppress")
                    when (index) {
                        is ValueIndex -> CBLDatabase_CreateValueIndex(
                            actual,
                            name.toFLString(this),
                            index.getActual(),
                            error
                        )
                        is FullTextIndex -> CBLDatabase_CreateFullTextIndex(
                            actual,
                            name.toFLString(this),
                            index.getActual(),
                            error
                        )
                        else -> error("Unhandled Index type ${index::class}")
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        wrapCBLError { error ->
            mustBeOpen {
                memScoped {
                    when (config) {
                        is ValueIndexConfiguration -> CBLDatabase_CreateValueIndex(
                            actual,
                            name.toFLString(this),
                            config.getActual(),
                            error
                        )
                        is FullTextIndexConfiguration -> CBLDatabase_CreateFullTextIndex(
                            actual,
                            name.toFLString(this),
                            config.getActual(),
                            error
                        )
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        wrapCBLError { error ->
            mustBeOpen {
                memScoped {
                    CBLDatabase_DeleteIndex(actual, name.toFLString(this), error)
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return wrapCBLError { error ->
            mustBeOpen {
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

    private fun <R> mustBeOpen(action: () -> R): R {
        return withLock {
            if (isClosed) {
                throw IllegalStateException("Attempt to perform an operation on a closed database.")
            }
            action()
        }
    }
}