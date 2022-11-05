@file:JvmName("DatabaseJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import com.couchbase.lite.UnitOfWork
import com.couchbase.lite.internal.CouchbaseLiteInternal
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toDate
import com.udobny.kmp.ext.toFile
import com.udobny.kmp.ext.toKotlinInstant
import kotlinx.datetime.Instant
import java.io.File

public actual class Database
internal constructor(actual: com.couchbase.lite.Database) :
    DelegatedClass<com.couchbase.lite.Database>(actual) {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(com.couchbase.lite.Database(name))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) :
            this(com.couchbase.lite.Database(name, config.actual))

    public actual companion object {

        public actual val log: Log by lazy { Log(com.couchbase.lite.Database.log) }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            com.couchbase.lite.Database.delete(name, directory?.toFile())
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            com.couchbase.lite.Database.exists(
                name,
                // TODO: remove CouchbaseLiteInternal.getRootDir() when nullable in Java SDK
                //  should be in 3.1
                //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/6
                directory?.let { File(it) } ?: CouchbaseLiteInternal.getRootDir()
            )

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            com.couchbase.lite.Database.copy(
                File(path),
                name,
                config?.actual ?: com.couchbase.lite.DatabaseConfiguration()
            )
        }
    }

    public actual val name: String
        get() = actual.name

    public actual val path: String?
        get() = actual.path

    public actual val count: Long
        get() = actual.count

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

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
    public actual fun <R> inBatch(work: Database.() -> R): R {
        var result: R? = null
        actual.inBatch(UnitOfWork {
            result = this.work()
        })
        return result!!
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
        actual.createIndex(name, config.actual)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteIndex(name: String) {
        actual.deleteIndex(name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean =
        actual.performMaintenance(type)
}
