package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDatabase
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.couchbase.lite.kmm.internal.testQueue
import com.couchbase.lite.kmm.internal.useTestQueue
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.wrapError
import kotlinx.cinterop.ObjCMethod
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSError
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_queue_t

public actual class Database
internal constructor(actual: CBLDatabase) :
    DelegatedClass<CBLDatabase>(actual) {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
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
        mustBeOpen()
        return actual.documentWithID(id)?.asDocument()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        mustBeOpen()
        throwError { error ->
            saveDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        mustBeOpen()
        return try {
            throwError { error ->
                saveDocument(document.actual, concurrencyControl.actual, error)
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
        mustBeOpen()
        return throwError { error ->
            saveDocument(document.actual, conflictHandler.convert(), error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        mustBeOpen()
        throwError { error ->
            deleteDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        mustBeOpen()
        return try {
            throwError { error ->
                deleteDocument(document.actual, concurrencyControl.actual, error)
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
        mustBeOpen()
        try {
            throwError { error ->
                purgeDocument(document.actual, error)
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
        mustBeOpen()
        throwError { error ->
            purgeDocumentWithID(id, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        mustBeOpen()
        throwError { error ->
            setDocumentExpirationWithID(id, expiration?.toNSDate(), error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? {
        mustBeOpen()
        return actual.getDocumentExpirationWithID(id)?.toKotlinInstant()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun inBatch(work: () -> Unit) {
        mustBeOpen()
        throwError { error ->
            inBatch(error, work)
        }
    }

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken {
        mustBeOpen()
        return DelegatedListenerToken(
            if (useTestQueue) {
                actual.addChangeListenerWithQueue(testQueue, listener.convert())
            } else {
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
        mustBeOpen()
        return DelegatedListenerToken(
            if (useTestQueue) {
                actual.addDocumentChangeListenerWithID(id, testQueue, listener.convert())
            } else {
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
        mustBeOpen()
        throwError { error ->
            delete(error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query {
        mustBeOpen()
        val actualQuery = throwError { error ->
            createQuery(query, error)
        }
        return DelegatedQuery(actualQuery!!)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> {
        mustBeOpen()
        return actual.indexes as List<String>
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        mustBeOpen()
        throwError { error ->
            createIndex(index.actual, name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        mustBeOpen()
        throwError { error ->
            createIndexWithConfig(config.actual, name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        mustBeOpen()
        throwError { error ->
            deleteIndexForName(name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        mustBeOpen()
        return throwError { error ->
            performMaintenance(type.actual, error)
        }
    }

    private fun mustBeOpen() {
        if (actual.isClosedLocked()) {
            throw IllegalStateException("Attempt to perform an operation on a closed database.")
        }
    }

    internal val isOpen: Boolean
        get() = !actual.isClosedLocked()
}

// TODO: replace with .def pending https://github.com/JetBrains/kotlin/pull/4894
@ObjCMethod("isClosedLocked", "@16@0:8")
private external fun CBLDatabase.isClosedLocked(): Boolean
