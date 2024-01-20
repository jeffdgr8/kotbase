/*
 * Copyright 2023 Jeff Lockhart
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

/**
 * A `Scope` represents a scope or namespace of collections.
 *
 * The scope implicitly exists when there is at least one collection created under the scope. The default scope is
 * exceptional in that it will always exist even when there are no collections under it.
 *
 * A `Scope` object remains valid until either the database is closed or the scope itself is invalidated as all
 * collections in the scope have been deleted.
 */
public expect class Scope {

    /**
     * Database
     */
    public val database: Database

    /**
     * The scope name.
     */
    public val name: String

    /**
     * Get all collections in the scope.
     */
    @Suppress("WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET")
    @get:Throws(CouchbaseLiteException::class)
    public val collections: Set<Collection>

    /**
     * Get the named collection for the scope.
     *
     * @param collectionName the name of the sought collection
     * @return the named collection or null
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollection(collectionName: String): Collection?

    public companion object
}

public val Scope.Companion.DEFAULT_NAME: String
    get() = "_default"
