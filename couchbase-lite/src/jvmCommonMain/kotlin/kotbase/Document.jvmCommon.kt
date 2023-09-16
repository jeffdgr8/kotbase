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

import kotbase.ext.toKotlinInstant
import kotlinx.datetime.Instant
import com.couchbase.lite.Document as CBLDocument

internal actual class DocumentPlatformState(
    internal val actual: CBLDocument
)

public actual open class Document
internal constructor(actual: CBLDocument) : Iterable<String> {

    internal actual val platformState = DocumentPlatformState(actual)

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual val id: String
        get() = actual.id

    public actual val revisionID: String?
        get() = actual.revisionID

    public actual val sequence: Long
        get() = actual.sequence

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual val count: Int
        get() = actual.count()

    public actual val keys: List<String>
        get() = actual.keys

    public actual fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: actual.getValue(key)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

    public actual fun getString(key: String): String? =
        actual.getString(key)

    public actual fun getNumber(key: String): Number? =
        actual.getNumber(key)

    public actual fun getInt(key: String): Int =
        actual.getInt(key)

    public actual fun getLong(key: String): Long =
        actual.getLong(key)

    public actual fun getFloat(key: String): Float =
        actual.getFloat(key)

    public actual fun getDouble(key: String): Double =
        actual.getDouble(key)

    public actual fun getBoolean(key: String): Boolean =
        actual.getBoolean(key)

    public actual fun getBlob(key: String): Blob? =
        actual.getBlob(key)?.asBlob()

    public actual fun getDate(key: String): Instant? =
        actual.getDate(key)?.toKotlinInstant()

    public actual open fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: actual.getArray(key)?.asArray()
                ?.also { collectionMap[key] = it }
    }

    public actual open fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: actual.getDictionary(key)?.asDictionary()
                ?.also { collectionMap[key] = it }
    }

    public actual fun toMap(): Map<String, Any?> =
        actual.toMap().delegateIfNecessary()

    public actual fun toJSON(): String? =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        actual.contains(key)

    actual override operator fun iterator(): Iterator<String> =
        actual.iterator()

    override fun equals(other: Any?): Boolean =
        actual == (other as? Document)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val Document.actual: CBLDocument
    get() = platformState.actual

internal fun CBLDocument.asDocument() = Document(this)
