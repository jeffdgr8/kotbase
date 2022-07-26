package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.isClosed
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.wrapError
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSError
import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

public actual class Database
internal constructor(actual: CBLDatabase) :
    DelegatedClass<CBLDatabase>(actual) {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            require(name.isNotEmpty()) { "db name must not be empty" }
            CBLDatabase(name, error)
        }
    )

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            CBLDatabase(name, config.actual, error)
        }
    )

    public actual companion object {

        public actual val log: Log by lazy {
            Log(CBLDatabase.log())
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            // Java SDK throws not found error
            if (!exists(name, directory ?: DatabaseConfiguration().getDirectory())) {
                throw CouchbaseLiteException(
                    "Database not found for delete",
                    CBLError.Domain.CBLITE,
                    CBLError.Code.NOT_FOUND
                )
            }
            wrapError(NSError::toCouchbaseLiteException) { error ->
                CBLDatabase.deleteDatabase(name, directory, error)
            }
        }

        public actual fun exists(name: String, directory: String): Boolean =
            CBLDatabase.databaseExists(name, directory)

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration) {
            wrapError(NSError::toCouchbaseLiteException) { error ->
                CBLDatabase.copyFromPath(path, name, config.actual, error)
            }
        }
    }

    public actual val name: String
        get() = actual.name

    public actual val path: String?
        get() = actual.path

    public actual val count: Long
        get() = actual.count.toLong()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

    public actual fun getDocument(id: String): Document? {
        return mustBeOpen {
            actual.documentWithID(id)?.asDocument()
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        throwError { error ->
            mustBeOpen {
                saveDocument(document.actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        return try {
            throwError { error ->
                mustBeOpen {
                    saveDocument(document.actual, concurrencyControl.actual, error)
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
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return throwError { error ->
            try {
                mustBeOpen {
                    saveDocument(document.actual, conflictHandler.convert(), error)
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
        throwError { error ->
            mustBeOpen {
                deleteDocument(document.actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            throwError { error ->
                mustBeOpen {
                    deleteDocument(document.actual, concurrencyControl.actual, error)
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
            throwError { error ->
                mustBeOpen {
                    purgeDocument(document.actual, error)
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
        throwError { error ->
            mustBeOpen {
                purgeDocumentWithID(id, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        throwError { error ->
            mustBeOpen {
                setDocumentExpirationWithID(id, expiration?.toNSDate(), error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        return mustBeOpen {
            actual.getDocumentExpirationWithID(id)?.toKotlinInstant()
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun inBatch(work: () -> Unit) {
        throwError { error ->
            mustBeOpen {
                inBatch(error, work)
            }
        }
    }

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken {
        return DelegatedListenerToken(
            mustBeOpen {
                actual.addChangeListener(listener.convert())
            }
        )
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListenerWithToken(token.actual)
    }

    public actual fun addDocumentChangeListener(
        id: String,
        listener: DocumentChangeListener
    ): ListenerToken {
        return DelegatedListenerToken(
            mustBeOpen {
                actual.addDocumentChangeListenerWithID(id, listener.convert())
            }
        )
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun close() {
        throwError { error ->
            close(error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        throwError { error ->
            mustBeOpen {
                delete(error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query {
        val actualQuery = throwError { error ->
            mustBeOpen {
                createQuery(query, error)
            }
        }
        return DelegatedQuery(actualQuery!!)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> {
        return mustBeOpen {
            actual.indexes as List<String>
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        throwError { error ->
            mustBeOpen {
                createIndex(index.actual, name, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        throwError { error ->
            mustBeOpen {
                createIndexWithConfig(config.actual, name, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        throwError { error ->
            mustBeOpen {
                deleteIndexForName(name, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return throwError { error ->
            mustBeOpen {
                performMaintenance(type.actual, error)
            }
        }
    }

    internal fun mustBeOpen() {
        mustBeOpen { }
    }

    internal inline fun <R> withLock(action: () -> R): R {
        // TODO: uses _mutex instead of self as lock in 3.1, use - (id) mutex
        objc_sync_enter(actual)
        return try {
            action()
        } finally {
            objc_sync_exit(actual)
        }
    }

    private fun <R> mustBeOpen(action: () -> R): R {
        return withLock {
            if (actual.isClosed()) {
                throw IllegalStateException("Attempt to perform an operation on a closed database.")
            }
            action()
        }
    }
}
