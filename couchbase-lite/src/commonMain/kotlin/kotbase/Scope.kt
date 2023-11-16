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

public expect class Scope {

    /**
     * The scope name.
     */
    public val name: String

    /**
     * Get all collections in the scope.
     *
     * @return a set of all collections in the scope
     */
    @Throws(CouchbaseLiteException::class)
    public fun getCollections(): Set<Any>

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
