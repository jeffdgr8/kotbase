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

import cocoapods.CouchbaseLite.CBLCollection
import cocoapods.CouchbaseLite.CBLScope
import kotbase.ext.wrapCBLError
import kotbase.internal.DelegatedClass

public actual class Scope
internal constructor(
    actual: CBLScope,
    internal actual val database: Database
) : DelegatedClass<CBLScope>(actual) {

    public actual val name: String
        get() = actual.name

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(): Set<Collection> {
        @Suppress("UNCHECKED_CAST")
        val collections = wrapCBLError { error ->
            actual.collections(error) as List<CBLCollection>
        }.asCollections(database)
        return collections.toSet()
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String): Collection? {
        return wrapCBLError { error ->
            actual.collectionWithName(collectionName, error)
        }?.asCollection(database)
    }

    public actual companion object
}

internal fun CBLScope.asScope(database: Database) = Scope(this, database)

internal fun Iterable<CBLScope>.asScopes(database: Database) = map { Scope(it, database) }.toSet()
