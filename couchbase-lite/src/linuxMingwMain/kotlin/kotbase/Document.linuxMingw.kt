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

import cnames.structs.CBLDocument
import kotbase.internal.DbContext
import kotbase.internal.fleece.*
import kotbase.util.identityHashCodeHex
import kotlinx.cinterop.CPointer
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual open class Document
internal constructor(
    actual: CPointer<CBLDocument>,
    database: Database?,
    release: Boolean = true
) : Iterable<String>, DictionaryInterface {

    private val memory = object {
        val actual = actual
        val release = release
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (it.release) CBLDocument_Release(it.actual)
    }

    internal val actual: CPointer<CBLDocument>
        get() = memory.actual

    internal var database: Database?
        get() = dbContext.database
        set(value) {
            dbContext.database = value
        }

    internal val dbContext = DbContext(database)

    internal fun willSave(db: Database) {
        dbContext.willSave(db)
    }

    internal open var properties: FLDict = CBLDocument_Properties(actual)!!
        set(value) {
            field = value
            CBLDocument_SetProperties(actual, value)
        }

    internal actual val collectionMap: MutableMap<String, Any> = mutableMapOf()

    public actual val collection: Collection?
        get() = CBLDocument_Collection(actual)?.asCollection(database!!)

    public actual val id: String
        get() = CBLDocument_ID(actual).toKString()!!

    public actual val revisionID: String?
        get() = if (dbContext.database != null) CBLDocument_RevisionID(actual).toKString() else null

    public actual val sequence: Long
        get() = CBLDocument_Sequence(actual).toLong()

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(CBLDocument_MutableCopy(actual)!!, database)

    actual override val count: Int
        get() = FLDict_Count(properties).toInt()

    actual override val keys: List<String>
        get() = properties.keys()

    protected fun getFLValue(key: String): FLValue? =
        properties.getValue(key)

    actual override fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: getFLValue(key)?.toNative(dbContext)
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

    actual override fun getString(key: String): String? =
        getFLValue(key)?.toKString()

    actual override fun getNumber(key: String): Number? =
        getFLValue(key)?.toNumber()

    actual override fun getInt(key: String): Int =
        getFLValue(key).toInt()

    actual override fun getLong(key: String): Long =
        getFLValue(key).toLong()

    actual override fun getFloat(key: String): Float =
        getFLValue(key).toFloat()

    actual override fun getDouble(key: String): Double =
        getFLValue(key).toDouble()

    actual override fun getBoolean(key: String): Boolean =
        getFLValue(key).toBoolean()

    actual override fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob(dbContext)

    actual override fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    actual override fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toArray(dbContext)
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toDictionary(dbContext)
                ?.also { collectionMap[key] = it }
    }

    actual override fun toMap(): Map<String, Any?> =
        properties.toMap(dbContext)

    actual override fun toJSON(): String =
        CBLDocument_CreateJSON(actual).toKString()!!

    actual override operator fun contains(key: String): Boolean =
        keys.contains(key)

    private var mutations: Long = 0

    protected fun mutate() {
        mutations++
    }

    actual override fun iterator(): Iterator<String> =
        DocumentIterator(keys.iterator(), mutations)

    private inner class DocumentIterator(
        private val iterator: Iterator<String>,
        private val mutations: Long
    ) : Iterator<String> {

        override fun hasNext(): Boolean = iterator.hasNext()

        override fun next(): String {
            if (this@Document.mutations != mutations) {
                throw ConcurrentModificationException("Document modified during iteration")
            }
            return iterator.next()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Document) return false

        val db = database
        val otherDb = other.database
        // Step 1: Check Database
        if (if (db == null) otherDb != null else db.path != otherDb?.path) {
            return false
        }

        // Step 2: Check document ID
        if (id != other.id) return false

        // Step 3: Check content
        return Dictionary(properties, dbContext, release = false) == Dictionary(other.properties, other.dbContext, release = false)
    }

    override fun hashCode(): Int {
        val db = database
        var result = 0
        if (db != null) {
            val path = db.path
            if (path != null) {
                result = path.hashCode()
            }
        }
        result = 31 * result + id.hashCode()
        result = 31 * result + Dictionary(properties, dbContext, release = false).hashCode()
        return result
    }

    internal var isDeleted = false

    override fun toString(): String {
        return buildString {
            append("Document{").append(identityHashCodeHex())
            append(id).append('@').append(revisionID)
            append('(').append(if (this@Document is MutableDocument) '+' else '.')
            append(if (isDeleted) '?' else '.').append("):")
            var first = true
            for (key in keys) {
                if (first) {
                    first = false
                } else {
                    append(',')
                }
                append(key).append("=>").append(getValue(key))
            }
            append('}')
        }
    }
}

internal fun CPointer<CBLDocument>.asDocument(database: Database?, release: Boolean = true) =
    Document(this, database, release)
