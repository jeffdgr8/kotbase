package com.couchbase.lite.kmp

import cnames.structs.CBLDatabase
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.fleece.toList
import com.couchbase.lite.kmp.internal.toExceptionNotNull
import com.couchbase.lite.kmp.internal.toKotlinInstant
import com.couchbase.lite.kmp.internal.wrapCBLError
import com.udobny.kmp.to
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.*
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class Database
internal constructor(internal val actual: CPointer<CBLDatabase>) {

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLDatabase_Release(it)
    }

    internal var isClosed = false

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(name, DatabaseConfiguration())

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(
        wrapCBLError { error ->
            CBLDatabase_Open(name.toFLString(), config.actual, error)!!
        }
    )

    public actual companion object {

        public actual val log: Log by lazy {
            Log()
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
        get() = CBLDatabase_Path(actual).toKString()

    public actual val count: Long
        get() = CBLDatabase_Count(actual).toLong()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(CBLDatabase_Config(actual))

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
        wrapCBLError { error ->
            mustBeOpen {
                CBLDatabase_SaveDocument(actual, document.actual, error)
            }
        }
        document.database = this
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        return try {
            mustBeOpen {
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
            if (e.getCode() == CBLError.Code.CONFLICT && e.getDomain() == CBLError.Domain.CBLITE) {
                // Java SDK doesn't throw exception on conflict, only returns false
                false
            } else {
                throw e
            }
        }
    }

    private var conflictHandler: StableRef<ConflictHandler>? = null

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return mustBeOpen {
            wrapCBLError { error ->
                try {
                    this.conflictHandler = StableRef.create(conflictHandler)
                    CBLDatabase_SaveDocumentWithConflictHandler(
                        actual,
                        document.actual,
                        staticCFunction { ref, document, oldDocument ->
                            ref.to<ConflictHandler>()(
                                MutableDocument(document!!),
                                oldDocument?.asDocument()
                            )
                        },
                        this.conflictHandler!!.asCPointer(),
                        error
                    ).also {
                        this.conflictHandler?.dispose()
                        this.conflictHandler = null
                        document.database = this
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
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_DeleteDocument(actual, document.actual, error)
            }
            document.database = null
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
                if (e.getCode() == CBLError.Code.CONFLICT && e.getDomain() == CBLError.Domain.CBLITE) {
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
            val purged = wrapCBLError { error ->
                CBLDatabase_PurgeDocument(actual, document.actual, error)
            }
            document.database = null
            if (!purged) {
                if (document.revisionID == null) {
                    throw CouchbaseLiteException("The document doesn't exist in the database.", CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND)
                }
            }
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
            val holder = DatabaseChangeListenerHolder(listener, this)
            val (index, stableRef) = addListener(changeListeners, holder)
            DelegatedListenerToken(
                CBLDatabase_AddChangeListener(
                    actual,
                    staticCFunction { ref, _, numDocs, docIds ->
                        val size = numDocs.toInt()
                        val documentIds = buildList(size) {
                            repeat(size) { i ->
                                add(docIds!![i].toKString()!!)
                            }
                        }
                        with(ref.to<DatabaseChangeListenerHolder>()) {
                            this.listener(DatabaseChange(database, documentIds))
                        }
                    },
                    stableRef
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
            ListenerTokenType.DATABASE -> removeListener(changeListeners, token.index)
            ListenerTokenType.DOCUMENT -> removeListener(documentChangeListeners, token.index)
            else -> error("${token.type} change listener can't be removed from Database instance")
        }
    }

    private val documentChangeListeners = mutableListOf<StableRef<DocumentChangeListenerHolder>?>()

    public actual fun addDocumentChangeListener(
        id: String,
        listener: DocumentChangeListener
    ): ListenerToken {
        return mustBeOpen {
            val holder = DocumentChangeListenerHolder(listener, this)
            val (index, stableRef) = addListener(documentChangeListeners, holder)
            DelegatedListenerToken(
                CBLDatabase_AddDocumentChangeListener(
                    actual,
                    id.toFLString(),
                    staticCFunction { ref, _, docId ->
                        with(ref.to<DocumentChangeListenerHolder>()) {
                            this.listener(DocumentChange(database, docId.toKString()!!))
                        }
                    },
                    stableRef
                )!!,
                ListenerTokenType.DOCUMENT,
                index
            )
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun close() {
        wrapCBLError { error ->
            withLock {
                isClosed = true
                CBLDatabase_Close(actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        wrapCBLError { error ->
            mustBeOpen {
                CBLDatabase_Delete(actual, error)
                isClosed = true
            }
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
                        else -> error("Unknown Index type ${index::class}")
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
                        else -> error("Unknown IndexConfiguration type ${config::class}")
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
