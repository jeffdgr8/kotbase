package com.couchbase.lite.kmp

import cnames.structs.CBLDatabase
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.fleece.toList
import com.couchbase.lite.kmp.internal.toKotlinInstant
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class Database
private constructor(internal val actual: CPointer<CBLDatabase>) {

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLDatabase_Release(it)
    }

    private var isClosed = false

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(name, DatabaseConfiguration())

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(
        wrapError { error ->
            CBLDatabase_Open(name.toFLString(), config.actual, error)!!
        }
    )

    public actual companion object {

        public actual val log: Log by lazy {
            Log(CBLDatabase.log())
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            // Java SDK throws not found error
            if (!exists(name, directory ?: DatabaseConfiguration().directory)) {
                throw CouchbaseLiteException(
                    "Database not found for delete",
                    CBLError.Domain.CBLITE,
                    CBLError.Code.NOT_FOUND
                )
            }
            wrapError { error ->
                CBL_DeleteDatabase(name.toFLString(), directory.toFLString(), error)
            }
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            CBL_DatabaseExists(name.toFLString(), directory.toFLString())

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            wrapError { error ->
                CBL_CopyDatabase(path.toFLString(), name.toFLString(), config?.actual, error)
            }
        }
    }

    public actual val name: String
        get() = CBLDatabase_Name(actual).toKString()!!

    public actual val path: String?
        get() = CBLDatabase_Path(actual).toKString()

    public actual val count: Long
        get() = CBLDatabase_Count(actual).toLong()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(CBLDatabase_Config(actual))

    public actual fun getDocument(id: String): Document? {
        return mustBeOpen {
            wrapError { error ->
                CBLDatabase_GetDocument(actual, id.toFLString(), error)?.asDocument()
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        wrapError { error ->
            mustBeOpen {
                CBLDatabase_SaveDocument(actual, document.actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        return try {
            wrapError { error ->
                mustBeOpen {
                    CBLDatabase_SaveDocumentWithConcurrencyControl(
                        actual,
                        document.actual,
                        concurrencyControl.actual,
                        error
                    )
                }
            }
        } catch (e: CouchbaseLiteException) {
            if (e.getCode() == CBLError.Code.CONFLICT && e.getDomain() == CBLError.Domain.CBLITE) {
                // Java SDK doesn't throw exception on conflict, only returns false
                false
            } else {
                throw e
            }
        }
    }

    private var conflictHandler: ConflictHandler? = null

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return wrapError { error ->
            try {
                mustBeOpen {
                    this.conflictHandler = conflictHandler
                    CBLDatabase_SaveDocumentWithConflictHandler(
                        actual,
                        document.actual,
                        staticCFunction { _, document, oldDocument ->
                            this.conflictHandler!!(
                                MutableDocument(document!!),
                                oldDocument?.asDocument()
                            )
                        },
                        null,
                        error
                    ).also {
                        this.conflictHandler = null
                    }
                }
            } catch (e: Exception) {
                throw CouchbaseLiteException(
                    "Conflict handler threw an exception",
                    e,
                    CBLError.Domain.CBLITE,
                    CBLError.Code.CONFLICT
                )
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        wrapError { error ->
            mustBeOpen {
                CBLDatabase_DeleteDocument(actual, document.actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            wrapError { error ->
                mustBeOpen {
                    CBLDatabase_DeleteDocumentWithConcurrencyControl(
                        actual,
                        document.actual,
                        concurrencyControl.actual,
                        error
                    )
                }
            }
        } catch (e: CouchbaseLiteException) {
            if (e.getCode() == CBLError.Code.CONFLICT && e.getDomain() == CBLError.Domain.CBLITE) {
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
            wrapError { error ->
                mustBeOpen {
                    CBLDatabase_PurgeDocument(actual, document.actual, error)
                }
            }
        } catch (e: CouchbaseLiteException) {
            // Java SDK ignores not found error, except for new document
            val isNew = document.revisionID == null
            if (isNew || e.getCode() != CBLError.Code.NOT_FOUND || e.getDomain() != CBLError.Domain.CBLITE) {
                throw e
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        wrapError { error ->
            mustBeOpen {
                CBLDatabase_PurgeDocumentByID(actual, id.toFLString(), error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        wrapError { error ->
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
        return wrapError { error ->
            mustBeOpen {
                CBLDatabase_GetDocumentExpiration(actual, id.toFLString(), error).toKotlinInstant()
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun <R> inBatch(work: Database.() -> R): R {
        return mustBeOpen {
            wrapError { error ->
                CBLDatabase_BeginTransaction(actual, error)
            }

            val result: R
            var commit = false
            try {
                result = this@Database.work()
                commit = true
            } finally {
                wrapError { error ->
                    CBLDatabase_EndTransaction(actual, commit, error)
                }
            }
            result
        }
    }

    private val changeListeners = mutableListOf<DatabaseChangeListener?>()

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken {
        return mustBeOpen {
            val index = addChangeListener(changeListeners, listener)
            DelegatedListenerToken(
                CBLDatabase_AddChangeListener(
                    actual,
                    staticCFunction { idx, db, numDocs, docIds ->
                        val documentIds = buildList {
                            for (i in 0 until numDocs.toInt()) {
                                add(docIds!![i].toKString()!!)
                            }
                        }
                        changeListeners[idx.toLong().toInt()]!!(
                            DatabaseChange(Database(db!!), documentIds)
                        )
                    },
                    index.toLong().toCPointer<CPointed>()
                )!!,
                ListenerTokenType.DATABASE,
                index
            )
        }
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        CBLListener_Remove(token.actual)
        when (token.type) {
            ListenerTokenType.DATABASE -> removeChangeListener(changeListeners, token.index)
            ListenerTokenType.DOCUMENT -> removeChangeListener(documentChangeListeners, token.index)
            else -> error("${token.type} change listener can't be removed from Database instance")
        }
    }

    private val documentChangeListeners = mutableListOf<DocumentChangeListener?>()

    public actual fun addDocumentChangeListener(
        id: String,
        listener: DocumentChangeListener
    ): ListenerToken {
        return mustBeOpen {
            val index = addChangeListener(documentChangeListeners, listener)
            DelegatedListenerToken(
                CBLDatabase_AddDocumentChangeListener(
                    actual,
                    id.toFLString(),
                    staticCFunction { idx, db, docId ->
                        documentChangeListeners[idx.toLong().toInt()]!!(
                            DocumentChange(Database(db!!), docId.toKString()!!)
                        )
                    },
                    index.toLong().toCPointer<CPointed>()
                )!!,
                ListenerTokenType.DOCUMENT,
                index
            )
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun close() {
        wrapError { error ->
            withLock {
                isClosed = true
                CBLDatabase_Close(actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        wrapError { error ->
            mustBeOpen {
                CBLDatabase_Delete(actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query {
        val actualQuery = memScoped {
            val errorPos = alloc<IntVar>()
            wrapError({
                toException(mapOf("position" to errorPos.value))
            }) { error ->
                mustBeOpen {
                    CBLDatabase_CreateQuery(
                        actual,
                        kCBLN1QLLanguage,
                        query.toFLString(),
                        errorPos.ptr,
                        error
                    )
                }
            }
        }
        return DelegatedQuery(actualQuery!!)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> {
        return mustBeOpen {
            CBLDatabase_GetIndexNames(actual)
                ?.toList() as List<String>? ?: emptyList()
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        wrapError { error ->
            mustBeOpen {
                memScoped {
                    when (index) {
                        is ValueIndex -> CBLDatabase_CreateValueIndex(
                            actual,
                            name.toFLString(),
                            index.getActual(this),
                            error
                        )
                        is FullTextIndex -> CBLDatabase_CreateFullTextIndex(
                            actual,
                            name.toFLString(),
                            index.getActual(this),
                            error
                        )
                        else -> error("Unknown Index type ${index::class}")
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        wrapError { error ->
            mustBeOpen {
                memScoped {
                    when (config) {
                        is ValueIndexConfiguration -> CBLDatabase_CreateValueIndex(
                            actual,
                            name.toFLString(),
                            config.getActual(this),
                            error
                        )
                        is FullTextIndexConfiguration -> CBLDatabase_CreateFullTextIndex(
                            actual,
                            name.toFLString(),
                            config.getActual(this),
                            error
                        )
                        else -> error("Unknown IndexConfiguration type ${config::class}")
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        wrapError { error ->
            mustBeOpen {
                CBLDatabase_DeleteIndex(actual, name.toFLString(), error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return wrapError { error ->
            mustBeOpen {
                CBLDatabase_PerformMaintenance(actual, type.actual, error)
            }
        }
    }

    internal fun mustBeOpen() {
        mustBeOpen { }
    }

    private val lock = Mutex()

    internal inline fun <R> withLock(crossinline action: () -> R): R {
        return runBlocking {
            lock.withLock {
                action()
            }
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
