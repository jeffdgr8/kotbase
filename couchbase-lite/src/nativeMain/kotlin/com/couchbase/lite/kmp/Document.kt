package com.couchbase.lite.kmp

import cnames.structs.CBLDocument
import com.couchbase.lite.kmp.internal.DbContext
import com.couchbase.lite.kmp.internal.fleece.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner

public actual open class Document
internal constructor(
    actual: CPointer<CBLDocument>,
    database: Database? = null
) : Iterable<String> {

    init {
        CBLDocument_Retain(actual)
    }

    internal open val actual: CPointer<CBLDocument> = actual

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLDocument_Release(it)
    }

    internal var database: Database?
        get() = dbContext.database
        set(value) {
            dbContext.database = value
        }

    internal val dbContext: DbContext = DbContext(database)

    internal open val properties: FLDict
        get() = CBLDocument_Properties(actual)!!

    public actual val id: String
        get() = CBLDocument_ID(actual).toKString()!!

    public actual val revisionID: String?
        get() = CBLDocument_RevisionID(actual).toKString()

    public actual val sequence: Long
        get() = CBLDocument_Sequence(actual).toLong()

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(CBLDocument_MutableCopy(actual)!!)

    public actual val count: Int
        get() = FLDict_Count(properties).toInt()

    public actual val keys: List<String>
        get() = properties.keys()

    protected fun getFLValue(key: String): FLValue? =
        properties.getValue(key)

    public actual open fun getValue(key: String): Any? =
        getFLValue(key)?.toNative(dbContext)

    public actual fun getString(key: String): String? =
        getFLValue(key)?.toKString()

    public actual fun getNumber(key: String): Number? =
        getFLValue(key)?.toNumber()

    public actual fun getInt(key: String): Int =
        getFLValue(key).toInt()

    public actual fun getLong(key: String): Long =
        getFLValue(key).toLong()

    public actual fun getFloat(key: String): Float =
        getFLValue(key).toFloat()

    public actual fun getDouble(key: String): Double =
        getFLValue(key).toDouble()

    public actual fun getBoolean(key: String): Boolean =
        getFLValue(key).toBoolean()

    public actual fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob(dbContext)

    public actual fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    public actual open fun getArray(key: String): Array? =
        getFLValue(key)?.toArray(dbContext)

    public actual open fun getDictionary(key: String): Dictionary? =
        getFLValue(key)?.toDictionary(dbContext)

    public actual fun toMap(): Map<String, Any?> =
        properties.toMap(dbContext)

    public actual open fun toJSON(): String? =
        FLValue_ToJSON(properties.reinterpret()).toKString()!!

    public actual operator fun contains(key: String): Boolean =
        keys.contains(key)

    actual override operator fun iterator(): Iterator<String> =
        keys.iterator()

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
        return Dictionary(properties, dbContext) == Dictionary(other.properties, other.dbContext)
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
        result = 31 * result + Dictionary(properties, dbContext).hashCode()
        return result
    }

    protected open val isMutable: Boolean = false

    override fun toString(): String {
        val buf = StringBuilder("Document{").append(super.toString())
                .append(id).append('@').append(revisionID)
                .append('(').append(if (isMutable) '+' else '.')
                //.append(if (isDeleted) '?' else '.').append("):")
        var first = true
        for (key in keys) {
            if (first) {
                first = false
            } else {
                buf.append(',')
            }
            buf.append(key).append("=>").append(getValue(key))
        }
        return buf.append('}').toString()
    }
}

internal fun CPointer<CBLDocument>.asDocument(database: Database? = null) =
    Document(this, database)
