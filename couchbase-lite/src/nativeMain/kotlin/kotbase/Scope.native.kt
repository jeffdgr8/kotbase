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

import cnames.structs.CBLScope
import kotbase.internal.fleece.iterator
import kotbase.internal.fleece.toFLString
import kotbase.internal.fleece.toKString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import libcblite.*

public actual class Scope
internal constructor(
    internal val actual: CPointer<CBLScope>,
    internal actual val database: Database
) {

    public actual val name: String
        get() = CBLScope_Name(actual).toKString()!!

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollections(): Set<Collection> {
        val names = wrapCBLError { error ->
            CBLScope_CollectionNames(actual, error)
        }
        return buildSet {
            memScoped {
                names?.iterator(this)?.forEach {
                    wrapCBLError { error ->
                        CBLScope_Collection(actual, FLValue_AsString(it), error)
                    }?.asCollection(database)?.let(::add)
                }
            }
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun getCollection(collectionName: String): Collection? {
        return wrapCBLError { error ->
            memScoped {
                CBLScope_Collection(actual, collectionName.toFLString(this), error)
            }
        }?.asCollection(database)
    }

    public actual companion object
}

internal fun CPointer<CBLScope>.asScope(database: Database) = Scope(this, database)
