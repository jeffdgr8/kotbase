package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLDatabase
import cocoapods.CouchbaseLite.isClosed
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.DelegatedClass
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

public actual class Database
internal constructor(actual: CBLDatabase) :
    DelegatedClass<CBLDatabase>(actual) {

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
        mustBeOpen {
            wrapCBLError { error ->
                actual.saveDocument(document.actual, error)
            }
        }
    }

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
            if (e.code == CBLError.Code.CONFLICT && e.domain == CBLError.Domain.CBLITE) {
                // Java SDK doesn't throw exception on conflict, only returns false
                false
            } else {
                throw e
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return mustBeOpen {
            wrapCBLError { error ->
                try {
                    actual.saveDocument(document.actual, conflictHandler.convert(), error)
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
                actual.deleteDocument(document.actual, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return try {
            mustBeOpen {
                wrapCBLError { error ->
                    actual.deleteDocument(document.actual, concurrencyControl.actual, error)
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

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.purgeDocumentWithID(id, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.setDocumentExpirationWithID(id, expiration?.toNSDate(), error)
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
    public actual fun <R> inBatch(work: Database.() -> R): R {
        return mustBeOpen {
            wrapCBLError { error ->
                var result: R? = null
                actual.inBatch(error) {
                    result = this@Database.work()
                }
                result
            }!!
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
        token as DelegatedListenerToken
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
        wrapCBLError { error ->
            actual.close(error)
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

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query {
        val actualQuery = mustBeOpen {
            wrapCBLError { error ->
                actual.createQuery(query, error)
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
        mustBeOpen {
            wrapCBLError { error ->
                actual.createIndex(index.actual, name, error)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        mustBeOpen {
            wrapCBLError { error ->
                actual.createIndexWithConfig(config.actual, name, error)
            }
        }
    }

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
