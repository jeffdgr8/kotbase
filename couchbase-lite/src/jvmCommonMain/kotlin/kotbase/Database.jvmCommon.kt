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

import com.couchbase.lite.UnitOfWork
import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotbase.base.DelegatedClass
import kotbase.ext.toDate
import kotbase.ext.toFile
import kotbase.ext.toKotlinInstant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.datetime.Instant
import java.io.File
import kotlin.coroutines.CoroutineContext
import com.couchbase.lite.Database as CBLDatabase
import com.couchbase.lite.DatabaseConfiguration as CBLDatabaseConfiguration

public actual class Database
internal constructor(actual: CBLDatabase) : DelegatedClass<CBLDatabase>(actual) {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(CBLDatabase(name))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) :
            this(CBLDatabase(name, config.actual))

    public actual companion object {

        init {
            internalInit()
        }

        public actual val log: Log by lazy { Log(CBLDatabase.log) }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            CBLDatabase.delete(name, directory?.toFile())
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            CBLDatabase.exists(
                name,
                // TODO: remove CouchbaseLiteInternal.getRootDir() when nullable in Java SDK
                //  should be in 3.1
                //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/6
                directory?.let { File(it) } ?: CouchbaseLiteInternal.getRootDir()
            )

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            CBLDatabase.copy(
                File(path),
                name,
                config?.actual ?: CBLDatabaseConfiguration()
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
        @Suppress("UNCHECKED_CAST")
        return result as R
    }

    public actual fun addChangeListener(listener: DatabaseChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert())

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: DatabaseChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListener(context[CoroutineDispatcher]?.asExecutor(), listener.convert(scope))
        return SuspendListenerToken(scope, token)
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(scope: CoroutineScope, listener: DatabaseChangeSuspendListener) {
        val token = actual.addChangeListener(
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            actual.removeChangeListener(token)
        }
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            actual.removeChangeListener(token.actual)
            token.scope.cancel()
        } else {
            actual.removeChangeListener(token)
        }
    }

    public actual fun addDocumentChangeListener(id: String, listener: DocumentChangeListener): ListenerToken =
        actual.addDocumentChangeListener(id, listener.convert())

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addDocumentChangeListener(
        id: String,
        context: CoroutineContext,
        listener: DocumentChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addDocumentChangeListener(
            id,
            context[CoroutineDispatcher]?.asExecutor(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addDocumentChangeListener(
        id: String,
        scope: CoroutineScope,
        listener: DocumentChangeSuspendListener
    ) {
        val token = actual.addDocumentChangeListener(
            id,
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            actual.removeChangeListener(token)
        }
    }

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
