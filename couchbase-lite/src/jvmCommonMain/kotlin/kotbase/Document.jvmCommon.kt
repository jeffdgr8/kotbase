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

import com.couchbase.lite.database
import kotbase.ext.toKotlinInstant
import kotbase.internal.DelegatedClass
import kotlin.time.Instant
import com.couchbase.lite.Document as CBLDocument

public actual open class Document
internal constructor(
    actual: CBLDocument,
    internal var collectionInternal: Collection?
) : DelegatedClass<CBLDocument>(actual), Iterable<String>, DictionaryInterface {

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual val collection: Collection?
        get() {
            if (collectionInternal == null) {
                val actualCollection = actual.collection ?: return null
                collectionInternal = Collection(actualCollection, actualCollection.database())
            }
            return collectionInternal
        }

    public actual val id: String
        get() = actual.id

    public actual val revisionID: String?
        get() = actual.revisionID

    public actual val sequence: Long
        get() = actual.sequence

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable(), collection)

    actual override val count: Int
        get() = actual.count()

    actual override val keys: List<String>
        get() = actual.keys

    actual override fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: actual.getValue(key)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

    actual override fun getString(key: String): String? =
        actual.getString(key)

    actual override fun getNumber(key: String): Number? =
        actual.getNumber(key)

    actual override fun getInt(key: String): Int =
        actual.getInt(key)

    actual override fun getLong(key: String): Long =
        actual.getLong(key)

    actual override fun getFloat(key: String): Float =
        actual.getFloat(key)

    actual override fun getDouble(key: String): Double =
        actual.getDouble(key)

    actual override fun getBoolean(key: String): Boolean =
        actual.getBoolean(key)

    actual override fun getBlob(key: String): Blob? =
        actual.getBlob(key)?.asBlob()

    actual override fun getDate(key: String): Instant? =
        actual.getDate(key)?.toKotlinInstant()

    actual override fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: actual.getArray(key)?.asArray()
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: actual.getDictionary(key)?.asDictionary()
                ?.also { collectionMap[key] = it }
    }

    actual override fun toMap(): Map<String, Any?> =
        actual.toMap().delegateIfNecessary()

    actual override fun toJSON(): String =
        actual.toJSON() ?: throw CouchbaseLiteException("Failed encoding document as JSON")

    actual override operator fun contains(key: String): Boolean =
        actual.contains(key)

    actual override fun iterator(): Iterator<String> =
        actual.iterator()
}

internal fun CBLDocument.asDocument(collection: Collection?) = Document(this, collection)
