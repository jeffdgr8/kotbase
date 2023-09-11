package kotbase

import kotbase.ext.toKotlinInstant
import kotbase.internal.DbContext
import kotlinx.datetime.Instant
import com.couchbase.lite.Array as CBLArray

internal actual class ArrayPlatformState(
    internal val actual: CBLArray
)

public actual open class Array
internal constructor(actual: CBLArray) : Iterable<Any?> {

    internal actual val platformState = ArrayPlatformState(actual)

    internal actual val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    internal actual open var dbContext: DbContext?
        get() = null
        set(_) {}

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count()

    public actual fun getValue(index: Int): Any? {
        return collectionMap[index]
            ?: actual.getValue(index)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    public actual fun getString(index: Int): String? =
        actual.getString(index)

    public actual fun getNumber(index: Int): Number? =
        actual.getNumber(index)

    public actual fun getInt(index: Int): Int =
        actual.getInt(index)

    public actual fun getLong(index: Int): Long =
        actual.getLong(index)

    public actual fun getFloat(index: Int): Float =
        actual.getFloat(index)

    public actual fun getDouble(index: Int): Double =
        actual.getDouble(index)

    public actual fun getBoolean(index: Int): Boolean =
        actual.getBoolean(index)

    public actual fun getBlob(index: Int): Blob? =
        actual.getBlob(index)?.asBlob()

    public actual fun getDate(index: Int): Instant? =
        actual.getDate(index)?.toKotlinInstant()

    public actual open fun getArray(index: Int): Array? {
        return getInternalCollection(index)
            ?: actual.getArray(index)?.asArray()
                ?.also { collectionMap[index] = it }
    }

    public actual open fun getDictionary(index: Int): Dictionary? {
        return getInternalCollection(index)
            ?: actual.getDictionary(index)?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    public actual fun toList(): List<Any?> =
        actual.toList().delegateIfNecessary()

    public actual fun toJSON(): String =
        actual.toJSON()

    actual override operator fun iterator(): Iterator<Any?> = object : Iterator<Any?> {

        private val itr = actual.iterator()

        override fun hasNext(): Boolean = itr.hasNext()

        override fun next(): Any? = itr.next()?.delegateIfNecessary()
    }

    override fun equals(other: Any?): Boolean =
        actual == (other as? Array)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val Array.actual: CBLArray
    get() = platformState.actual

internal fun CBLArray.asArray() = Array(this)
