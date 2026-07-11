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

import cocoapods.CouchbaseLite.*
import kotbase.internal.DelegatedClass
import kotbase.ext.wrapCBLError
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.experimental.ExperimentalObjCRefinement

public actual class Database
internal constructor(actual: CBLDatabase) : DelegatedClass<CBLDatabase>(actual), AutoCloseable {

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

        @Throws(CouchbaseLiteException::class)
        public actual fun delete(name: String, directory: String?) {
            // Java SDK throws not found error
            if (!exists(name, directory ?: DatabaseConfiguration(null).directory)) {
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

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(actual.config)

    actual override fun close() {
        withLock {
            wrapCBLError { error ->
                actual.close(error)
            }
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

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val scopes: Set<Scope>
        get() {
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                actual.scopes(error) as List<CBLScope>
            }.asScopes(this)
        }

    /**
     * Get scope names that have at least one collection.
     * Note: the default scope is exceptional as it will always be listed even though there are no collections
     * under it.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun scopes(): Set<Scope> = scopes

    @Throws(CouchbaseLiteException::class)
    public actual fun getScope(name: String): Scope? {
        return wrapCBLError { error ->
            actual.scopeWithName(name, error)
        }?.asScope(this)
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val defaultScope: Scope
        get() {
            return wrapCBLError { error ->
                actual.defaultScope(error)
            }!!.asScope(this)
        }

    /**
     * Get the default scope.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun defaultScope(): Scope = defaultScope

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(name: String): Collection {
        return wrapCBLError { error ->
            actual.createCollectionWithName(name, null, error)
        }!!.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(collectionName: String, scopeName: String?): Collection {
        return wrapCBLError { error ->
            actual.createCollectionWithName(collectionName, scopeName, error)
        }!!.asCollection(this)
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val collections: Set<Collection>
        get() {
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                actual.collections(null, error) as List<CBLCollection>
            }.asCollections(this)
        }

    /**
     * Get all collections in the default scope.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun collections(): Set<Collection> = collections

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(scopeName: String?): Set<Collection> {
        return wrapCBLError { error ->
            @Suppress("UNCHECKED_CAST")
            actual.collections(scopeName, error) as List<CBLCollection>
        }.asCollections(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(name: String): Collection? {
        return wrapCBLError { error ->
            actual.collectionWithName(name, null, error)
        }?.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String, scopeName: String?): Collection? {
        return wrapCBLError { error ->
            actual.collectionWithName(collectionName, scopeName, error)
        }?.asCollection(this)
    }

    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val defaultCollection: Collection by lazy {
        wrapCBLError { error ->
            actual.defaultCollection(error)
        }!!.asCollection(this)
    }

    /**
     * Get the default collection.
     */
    // For Objective-C/Swift throws
    @Throws(CouchbaseLiteException::class)
    public fun defaultCollection(): Collection = defaultCollection

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(name: String) {
        wrapCBLError { error ->
            actual.deleteCollectionWithName(name, null, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(collectionName: String, scopeName: String?) {
        wrapCBLError { error ->
            actual.deleteCollectionWithName(collectionName, scopeName, error)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun <R> inBatch(work: Database.() -> R): R {
        return mustBeOpen {
            @Suppress("UNCHECKED_CAST")
            wrapCBLError { error ->
                var result: R? = null
                actual.inBatch(error) {
                    result = this@Database.work()
                }
                result
            } as R
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

    private val lock = reentrantLock()

    private inline fun <R> withLock(action: () -> R): R =
        lock.withLock(action)

    private fun <R> mustBeOpen(action: () -> R): R {
        return withLock {
            if (actual.isClosed()) {
                throw CouchbaseLiteError("Attempt to perform an operation on a closed database or a deleted collection.")
            }
            action()
        }
    }
}
