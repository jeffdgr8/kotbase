package com.couchbase.lite.kmm

import com.couchbase.lite.FullTextIndexConfiguration
import com.couchbase.lite.UnitOfWork
import com.couchbase.lite.ValueIndexConfiguration
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toDate
import com.udobny.kmm.ext.toKotlinInstant
import kotlinx.datetime.Instant

public actual class Database
internal constructor(actual: com.couchbase.lite.Database) :
    DelegatedClass<com.couchbase.lite.Database>(actual) {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(com.couchbase.lite.Database(name))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) :
            this(com.couchbase.lite.Database(name, config))

    public actual companion object {

        public actual val log: Log by lazy { Log(com.couchbase.lite.Database.log) }
    }

    public actual val name: String
        get() = actual.name

    public actual val path: String?
        get() = actual.path

    public actual val count: Long
        get() = actual.count

    public actual val config: DatabaseConfiguration
        get() = actual.config

    public actual fun getDocument(id: String): Document? =
        actual.getDocument(id)?.asDocument()

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument) {
        actual.save(document.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun save(
        document: MutableDocument,
        concurrencyControl: ConcurrencyControl
    ): Boolean =
        actual.save(document.actual, concurrencyControl)

    @Throws(CouchbaseLiteException::class)
    public actual fun save(document: MutableDocument, conflictHandler: ConflictHandler): Boolean =
        actual.save(document.actual, conflictHandler.convert())

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document) {
        actual.delete(document.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete(document: Document, concurrencyControl: ConcurrencyControl): Boolean =
        actual.delete(document.actual, concurrencyControl)

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(document: Document) {
        actual.purge(document.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun purge(id: String) {
        actual.purge(id)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun setDocumentExpiration(id: String, expiration: Instant?) {
        actual.setDocumentExpiration(id, expiration?.toDate())
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getDocumentExpiration(id: String): Instant? =
        actual.getDocumentExpiration(id)?.toKotlinInstant()

    @Throws(CouchbaseLiteException::class)
    public actual fun inBatch(work: () -> Unit) {
        actual.inBatch(UnitOfWork { work() })
    }

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert())

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListener(token)
    }

    public actual fun addDocumentChangeListener(
        id: String,
        listener: DocumentChangeListener
    ): ListenerToken =
        actual.addDocumentChangeListener(id, listener.convert())

    @Throws(CouchbaseLiteException::class)
    public actual fun close() {
        actual.close()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        actual.delete()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query =
        DelegatedQuery(actual.createQuery(query))

    @Throws(CouchbaseLiteException::class)
    public actual fun getIndexes(): List<String> =
        actual.indexes

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, index: Index) {
        actual.createIndex(name, index.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createIndex(name: String, config: IndexConfiguration) {
        // TODO: casting required until actual type com.couchbase.lite.IndexConfiguration is visible
        // https://forums.couchbase.com/t/can-indexconfiguration-be-made-public/33772
        when (val actualConfig = config.actual) {
            is FullTextIndexConfiguration -> actual.createIndex(name, actualConfig)
            is ValueIndexConfiguration -> actual.createIndex(name, actualConfig)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        actual.deleteIndex(name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean =
        actual.performMaintenance(type)
}
