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
import kotbase.internal.DelegatedClass
import kotbase.ext.toFile
import java.io.File
import com.couchbase.lite.Database as CBLDatabase
import com.couchbase.lite.DatabaseConfiguration as CBLDatabaseConfiguration

public actual class Database
internal constructor(actual: CBLDatabase) : DelegatedClass<CBLDatabase>(actual), AutoCloseable {

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(CBLDatabase(name))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(CBLDatabase(name, config.actual))

    public actual companion object {

        init {
            internalInit()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            CBLDatabase.delete(name, directory?.toFile())
        }

        public actual fun exists(name: String, directory: String?): Boolean =
            CBLDatabase.exists(
                name,
                directory?.let { File(it) }
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

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

    @Throws(CouchbaseLiteException::class)
    actual override fun close() {
        actual.close()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        actual.delete()
    }

    @get:Throws(CouchbaseLiteException::class)
    public actual val scopes: Set<Scope>
        get () = actual.scopes.asScopes(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getScope(name: String): Scope? =
        actual.getScope(name)?.asScope(this)

    @get:Throws(CouchbaseLiteException::class)
    public actual val defaultScope: Scope
        get() = Scope(actual.defaultScope, this)

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(name: String): Collection =
        Collection(actual.createCollection(name), this)

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(collectionName: String, scopeName: String?): Collection =
        Collection(actual.createCollection(collectionName, scopeName), this)

    @get:Throws(CouchbaseLiteException::class)
    public actual val collections: Set<Collection>
        get() = actual.collections.asCollections(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(scopeName: String?): Set<Collection> =
        actual.getCollections(scopeName).asCollections(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(name: String): Collection? =
        actual.getCollection(name)?.asCollection(this)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String, scopeName: String?): Collection? =
        actual.getCollection(collectionName, scopeName)?.asCollection(this)

    @get:Throws(CouchbaseLiteException::class)
    public actual val defaultCollection: Collection by lazy {
        actual.defaultCollection.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(name: String) {
        actual.deleteCollection(name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(collectionName: String, scopeName: String?) {
        actual.deleteCollection(collectionName, scopeName)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun <R> inBatch(work: Database.() -> R): R {
        var result: R? = null
        actual.inBatch(UnitOfWork {
            result = this.work()
        })
        @Suppress("UNCHECKED_CAST")
        return result as R
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String): Query =
        DelegatedQuery(actual.createQuery(query))

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean =
        actual.performMaintenance(type)
}
