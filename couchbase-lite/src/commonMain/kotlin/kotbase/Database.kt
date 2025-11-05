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

import kotlinx.coroutines.CoroutineScope
import kotlin.time.Instant
import kotlin.coroutines.CoroutineContext

/**
 * A Couchbase Lite database.
 */
public expect class Database : AutoCloseable {

    /**
     * Construct a Database with a given name and the default config.
     * If the database does not yet exist it will be created.
     *
     * @param name The name of the database: May NOT contain capital letters!
     * @throws CouchbaseLiteException if any error occurs during the open operation.
     */
    @Throws(CouchbaseLiteException::class)
    public constructor(name: String)

    /**
     * Construct a Database with a given name and database config.
     * If the database does not yet exist, it will be created, unless the `readOnly` option is used.
     *
     * @param name   The name of the database: May NOT contain capital letters!
     * @param config The database config.
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the open operation.
     */
    @Throws(CouchbaseLiteException::class)
    public constructor(name: String, config: DatabaseConfiguration)

    public companion object {

        /**
         * Deletes a database of the given name in the given directory.
         *
         * @param name      the database's name
         * @param directory the directory containing the database: the database's parent directory.
         * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
         */
        @Throws(CouchbaseLiteException::class)
        public fun delete(name: String, directory: String? = null)

        /**
         * Checks whether a database of the given name exists in the given directory or not.
         *
         * @param name      the database's name
         * @param directory the path where the database is located. If null, the default db directory will be used.
         * @return true if exists, false otherwise.
         */
        public fun exists(name: String, directory: String? = null): Boolean

        /**
         * Make a copy of a database in a new location.
         * It is recommended that this method not be used on an open database.
         *
         * @param path   path to the existing db file
         * @param name   the name of the new DB
         * @param config a config with the new location
         * @throws CouchbaseLiteException on copy failure
         */
        @Throws(CouchbaseLiteException::class)
        public fun copy(path: String, name: String, config: DatabaseConfiguration? = null)
    }

    /**
     * The database name
     */
    public val name: String

    /**
     * The database's absolute path or null if the database is closed.
     */
    public val path: String?

    /**
     * A READONLY copy of the database configuration.
     */
    public val config: DatabaseConfiguration

    /**
     * Closes a database.
     * Closing a database will stop all replicators, live queries and all listeners attached to it.
     *
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    override fun close()

    /**
     * Deletes a database.
     * Deleting a database will stop all replicators, live queries and all listeners attached to it.
     * Although attempting to close a closed database is not an error, attempting to delete a closed database is.
     *
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun delete()

    /**
     * Get scope names that have at least one collection.
     * Note: the default scope is exceptional as it will always be listed even though there are no collections
     * under it.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val scopes: Set<Scope>

    /**
     * Get a scope object by name. As the scope cannot exist by itself without having a collection,
     * the null value will be returned if there are no collections under the given scope’s name.
     * Note: The default scope is exceptional, and it will always be returned.
     */
    @Throws(CouchbaseLiteException::class)
    public fun getScope(name: String): Scope?

    /**
     * Get the default scope.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val defaultScope: Scope

    /**
     * Create a named collection in the default scope.
     * If the collection already exists, the existing collection will be returned.
     *
     * @param name the scope in which to create the collection
     * @return the named collection in the default scope
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createCollection(name: String): Collection

    /**
     * Create a named collection in the specified scope.
     * If the collection already exists, the existing collection will be returned.
     *
     * @param collectionName the name of the new collection
     * @param scopeName      the scope in which to create the collection
     * @return the named collection in the default scope
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun createCollection(collectionName: String, scopeName: String?): Collection

    /**
     * Get all collections in the default scope.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val collections: Set<Collection>

    /**
     * Get all collections in the named scope.
     *
     * @param scopeName the scope name
     * @return the collections in the named scope
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollections(scopeName: String?): Set<Collection>

    /**
     * Get a collection in the default scope by name.
     * If the collection doesn't exist, the function will return null.
     *
     * @param name the collection to find
     * @return the named collection or null
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollection(name: String): Collection?

    /**
     * Get a collection in the specified scope by name.
     * If the collection doesn't exist, the function will return null.
     *
     * @param collectionName the collection to find
     * @param scopeName      the scope in which to create the collection
     * @return the named collection or null
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollection(collectionName: String, scopeName: String?): Collection?

    /**
     * Get the default collection.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val defaultCollection: Collection

    /**
     * Delete a collection by name in the default scope. If the collection doesn't exist, the operation
     * will do nothing. Note: the default collection cannot be deleted.
     *
     * @param name the collection to be deleted
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun deleteCollection(name: String)

    /**
     * Delete a collection by name in the specified scope. If the collection doesn't exist, the operation
     * will do nothing. Note: the default collection cannot be deleted.
     *
     * @param collectionName the collection to be deleted
     * @param scopeName      the scope from which to delete the collection
     * @throws CouchbaseLiteException on failure
     */
    @Throws(CouchbaseLiteException::class)
    public fun deleteCollection(collectionName: String, scopeName: String?)

    /**
     * Runs a group of database operations in a batch. Use this when performing bulk write operations
     * like multiple inserts/updates; it saves the overhead of multiple database commits, greatly
     * improving performance.
     *
     * @param work a unit of work that may terminate abruptly (with an exception)
     * @throws CouchbaseLiteException Throws an exception if any error occurs during the operation.
     */
    @Throws(CouchbaseLiteException::class)
    public fun <R> inBatch(work: Database.() -> R): R

    /**
     * Create a SQL++ query.
     *
     * @param query a valid SQL++ query
     * @return the Query object
     */
    @Throws(CouchbaseLiteException::class)
    public fun createQuery(query: String): Query

    /**
     * Perform database maintenance.
     */
    @Throws(CouchbaseLiteException::class)
    public fun performMaintenance(type: MaintenanceType): Boolean
}
