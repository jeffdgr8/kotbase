package kotbase

import cnames.structs.CBLDocument
import kotbase.internal.DbContext
import kotbase.internal.fleece.*
import kotbase.util.identityHashCodeHex
import kotlinx.cinterop.CPointer
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner
import kotlin.reflect.safeCast

public actual open class Document
internal constructor(
    actual: CPointer<CBLDocument>,
    database: Database?
) : Iterable<String> {

    private val memory = object {
        val actual: CPointer<CBLDocument> = actual
        var properties: FLDict = CBLDocument_Properties(actual)!!
    }

    init {
        CBLDocument_Retain(actual)
        FLDict_Retain(memory.properties)
    }

    public open val actual: CPointer<CBLDocument> = actual

    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLDocument_Release(it.actual)
        FLDict_Release(it.properties)
    }

    internal var database: Database?
        get() = dbContext.database
        set(value) {
            dbContext.database = value
        }

    internal val dbContext: DbContext = DbContext(database)

    internal fun willSave(db: Database) {
        dbContext.willSave(db)
    }

    internal open var properties: FLDict = CBLDocument_Properties(actual)!!
        set(value) {
            FLDict_Release(field)
            field = value
            CBLDocument_SetProperties(actual, value)
            FLDict_Retain(value)
            memory.properties = value
        }

    protected val collectionMap: MutableMap<String, Any> = mutableMapOf()

    protected inline fun <reified T : Any> getInternalCollection(key: String): T? =
        T::class.safeCast(collectionMap[key])

    public actual val id: String
        get() = CBLDocument_ID(actual).toKString()!!

    public actual val revisionID: String?
        get() = if (dbContext.database != null) CBLDocument_RevisionID(actual).toKString() else null

    public actual val sequence: Long
        get() = CBLDocument_Sequence(actual).toLong()

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(CBLDocument_MutableCopy(actual)!!, database)

    public actual val count: Int
        get() = FLDict_Count(properties).toInt()

    public actual val keys: List<String>
        get() = properties.keys()

    protected fun getFLValue(key: String): FLValue? =
        properties.getValue(key)

    public actual open fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: getFLValue(key)?.toNative(dbContext)
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
    }

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

    public actual open fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob(dbContext)

    public actual fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    public actual open fun getArray(key: String): Array? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toArray(dbContext)
                ?.also { collectionMap[key] = it }
    }

    public actual open fun getDictionary(key: String): Dictionary? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toDictionary(dbContext)
                ?.also { collectionMap[key] = it }
    }

    public actual fun toMap(): Map<String, Any?> =
        properties.toMap(dbContext)

    public actual open fun toJSON(): String? =
        CBLDocument_CreateJSON(actual).toKString()

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
        val buf = StringBuilder("Document{").append(identityHashCodeHex())
            .append(id).append('@').append(revisionID)
            .append('(').append(if (isMutable) '+' else '.')
        //    .append(if (isDeleted) '?' else '.').append("):")
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

internal fun CPointer<CBLDocument>.asDocument(database: Database?) =
    Document(this, database)