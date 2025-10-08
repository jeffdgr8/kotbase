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
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Scope
internal constructor(
    internal val actual: CPointer<CBLScope>,
    public actual val database: Database
) {

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLScope_Release(it)
    }

    public actual val name: String
        get() = CBLScope_Name(actual).toKString()!!

    @Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT") // https://youtrack.jetbrains.com/issue/KT-63047
    //@get:Throws(CouchbaseLiteException::class)
    public actual val collections: Set<Collection>
        get() {
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
                FLMutableArray_Release(names)
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

    override fun toString(): String =
        "${database.name}.$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scope) return false
        // don't use == here! The database must be the exact same instance.
        return database === other.database && name == other.name
    }

    override fun hashCode(): Int =
        arrayOf(name, database).contentHashCode()

    public actual companion object
}

internal fun CPointer<CBLScope>.asScope(database: Database) = Scope(this, database)
