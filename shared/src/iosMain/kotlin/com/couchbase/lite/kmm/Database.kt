package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDatabase
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.wrapError
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSError

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
    }

    public actual val name: String
        get() = actual.name

    public actual val path: String?
        get() = actual.path

    public actual val count: Long
        get() = actual.count.toLong()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

    public actual fun getDocument(id: String): Document? =
        actual.documentWithID(id)?.asDocument()

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        throwError { error ->
            saveDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean {
        return throwError { error ->
            saveDocument(document.actual, concurrencyControl.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean {
        return throwError { error ->
            saveDocument(document.actual, conflictHandler.convert(), error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        throwError { error ->
            deleteDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean {
        return throwError { error ->
            deleteDocument(document.actual, concurrencyControl.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        throwError { error ->
            purgeDocument(document.actual, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        throwError { error ->
            purgeDocumentWithID(id, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        throwError { error ->
            setDocumentExpirationWithID(id, expiration?.toNSDate(), error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? =
        actual.getDocumentExpirationWithID(id)?.toKotlinInstant()

    @Throws(CouchbaseLiteException::class)
    public actual fun inBatch(work: () -> Unit) {
        throwError { error ->
            inBatch(error, work)
        }
    }

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken =
        DelegatedListenerToken(actual.addChangeListener(listener.convert()))

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListenerWithToken(token.actual)
    }

    public actual fun addDocumentChangeListener(
        id: String,
        listener: DocumentChangeListener
    ): ListenerToken =
        DelegatedListenerToken(actual.addDocumentChangeListenerWithID(id, listener.convert()))

    @Throws(CouchbaseLiteException::class)
    public actual fun close() {
        throwError { error ->
            close(error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        throwError { error ->
            delete(error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query {
        val actualQuery = throwError { error ->
            createQuery(query, error)
        }
        return DelegatedQuery(actualQuery!!)
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> =
        actual.indexes as List<String>

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        throwError { error ->
            createIndex(index.actual, name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        throwError { error ->
            createIndexWithConfig(config.actual, name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        throwError { error ->
            deleteIndexForName(name, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return throwError { error ->
            performMaintenance(type.actual, error)
        }
    }
}
