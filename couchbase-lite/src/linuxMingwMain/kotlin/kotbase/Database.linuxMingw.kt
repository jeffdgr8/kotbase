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

import cnames.structs.CBLDatabase
import cnames.structs.CBLQuery
import kotbase.internal.fleece.iterator
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.toExceptionNotNull
import kotbase.internal.wrapCBLError
import kotbase.util.identityHashCodeHex
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Database
internal constructor(
    internal val actual: CPointer<CBLDatabase>,
    private val _config: DatabaseConfiguration
) : AutoCloseable {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLDatabase_Release(it)
    }

    internal var isClosed = false

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String) : this(name, DatabaseConfiguration(null))

    @Throws(CouchbaseLiteException::class)
    public actual constructor(name: String, config: DatabaseConfiguration) : this(
        try {
            wrapCBLError { error ->
                memScoped {
                    CBLDatabase_Open(name.toFLString(this), config.actual, error)
                }
            }!!
        } catch (e: CouchbaseLiteException) {
            if (e.code == CBLError.Code.INVALID_PARAMETER && e.domain == CBLError.Domain.CBLITE) {
                throw IllegalArgumentException("Invalid parameter", e)
            } else {
                throw e
            }
        },
        config.also {
            it.readonly = true
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
                memScoped {
                    CBL_DeleteDatabase(name.toFLString(this), directory.toFLString(this), error)
                }
            }
        }

        public actual fun exists(name: String, directory: String?): Boolean {
            return memScoped {
                CBL_DatabaseExists(name.toFLString(this), directory.toFLString(this))
            }
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun copy(path: String, name: String, config: DatabaseConfiguration?) {
            wrapCBLError { error ->
                memScoped {
                    CBL_CopyDatabase(
                        path.toFLString(this),
                        name.toFLString(this),
                        config?.actual,
                        error
                    )
                }
            }
        }
    }

    public actual val name: String
        get() = CBLDatabase_Name(actual).toKString()!!

    public actual val path: String?
        get() = if (isClosed) null else CBLDatabase_Path(actual).toKString()

    public actual val config: DatabaseConfiguration
        get() = DatabaseConfiguration(_config)

    actual override fun close() {
        withLock {
            wrapCBLError { error ->
                CBLDatabase_Close(actual, error)
            }
            isClosed = true
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun delete() {
        mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_Delete(actual, error)
            }
            isClosed = true
        }
    }

    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val scopes: Set<Scope>
        get() {
            val names = wrapCBLError { error ->
                CBLDatabase_ScopeNames(actual, error)
            }
            return buildSet {
                memScoped {
                    names?.iterator(this)?.forEach {
                        wrapCBLError { error ->
                            CBLDatabase_Scope(actual, FLValue_AsString(it), error)
                        }?.asScope(this@Database)?.let(::add)
                    }
                }
                FLMutableArray_Release(names)
            }
        }

    @Throws(CouchbaseLiteException::class)
    public actual fun getScope(name: String): Scope? {
        return wrapCBLError { error ->
            memScoped {
                CBLDatabase_Scope(actual, name.toFLString(this), error)
            }
        }?.asScope(this)
    }

    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val defaultScope: Scope
        get() {
            return wrapCBLError { error ->
                CBLDatabase_DefaultScope(actual, error)
            }!!.asScope(this)
        }

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(name: String): Collection =
        createCollection(name, defaultScope.name)

    @Throws(CouchbaseLiteException::class)
    public actual fun createCollection(collectionName: String, scopeName: String?): Collection {
        return wrapCBLError { error ->
            memScoped {
                CBLDatabase_CreateCollection(
                    actual,
                    collectionName.toFLString(this),
                    scopeName.toFLString(this),
                    error
                )
            }
        }!!.asCollection(this)
    }

    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val collections: Set<Collection>
        get() = getCollections(defaultScope.name)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(scopeName: String?): Set<Collection> {
        return memScoped {
            val scope = scopeName.toFLString(this)
            val names = wrapCBLError { error ->
                CBLDatabase_CollectionNames(actual, scope, error)
            }
            buildSet {
                memScoped {
                    names?.iterator(this)?.forEach {
                        wrapCBLError { error ->
                            CBLDatabase_Collection(actual, FLValue_AsString(it), scope, error)
                        }?.asCollection(this@Database)?.let(::add)
                    }
                }
                FLMutableArray_Release(names)
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(name: String): Collection? =
        getCollection(name, defaultScope.name)

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String, scopeName: String?): Collection? {
        return wrapCBLError { error ->
            memScoped {
                CBLDatabase_Collection(
                    actual,
                    collectionName.toFLString(this),
                    scopeName.toFLString(this),
                    error
                )
            }
        }?.asCollection(this)
    }

    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val defaultCollection: Collection by lazy {
        wrapCBLError { error ->
            CBLDatabase_DefaultCollection(actual, error)
        }!!.asCollection(this)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(name: String) {
        deleteCollection(name, defaultScope.name)
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun deleteCollection(collectionName: String, scopeName: String?) {
        wrapCBLError { error ->
            memScoped {
                CBLDatabase_DeleteCollection(
                    actual,
                    collectionName.toFLString(this),
                    scopeName.toFLString(this),
                    error
                )
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
                    memScoped {
                        CBLDatabase_CreateQuery(
                            actual,
                            language,
                            queryString.toFLString(this),
                            errorPos.ptr,
                            error
                        )!!
                    }
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun performMaintenance(type: MaintenanceType): Boolean {
        return mustBeOpen {
            wrapCBLError { error ->
                CBLDatabase_PerformMaintenance(actual, type.actual, error)
            }
        }
    }

    internal fun mustBeOpen() {
        mustBeOpen { }
    }

    private val lock = reentrantLock()

    internal inline fun <R> withLock(crossinline action: () -> R): R {
        return lock.withLock {
            action()
        }
    }

    private fun <R> mustBeOpen(action: () -> R): R {
        return withLock {
            if (isClosed) {
                throw CouchbaseLiteError("Attempt to perform an operation on a closed database or a deleted collection.")
            }
            action()
        }
    }

    override fun toString(): String {
        return buildString {
            append("Database{@${identityHashCodeHex()}: '$name")
            if (config.isFullSync) append("!")
            append("'}")
        }
    }

    override fun hashCode(): Int =
        name.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Database) return false
        return path == other.path && name == other.name
    }
}
