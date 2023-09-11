package kotbase

import cocoapods.CouchbaseLite.CBLArray
import kotbase.ext.asNumber
import kotbase.internal.DbContext
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

internal actual class ArrayPlatformState(
    internal val actual: CBLArray
)

public actual open class Array
internal constructor(actual: CBLArray) : Iterable<Any?> {

    internal actual val platformState: ArrayPlatformState = ArrayPlatformState(actual)

    internal actual val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    internal actual open var dbContext: DbContext?
        get() = null
        set(_) {}

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    public actual fun getValue(index: Int): Any? {
        checkIndex(index)
        return collectionMap[index]
            ?: actual.valueAtIndex(index.convert())?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    public actual fun getString(index: Int): String? {
        checkIndex(index)
        return actual.stringAtIndex(index.convert())
    }

    public actual fun getNumber(index: Int): Number? {
        checkIndex(index)
        return actual.numberAtIndex(index.convert())?.asNumber()
    }

    public actual fun getInt(index: Int): Int {
        checkIndex(index)
        return actual.integerAtIndex(index.convert()).toInt()
    }

    public actual fun getLong(index: Int): Long {
        checkIndex(index)
        return actual.longLongAtIndex(index.convert())
    }

    public actual fun getFloat(index: Int): Float {
        checkIndex(index)
        return actual.floatAtIndex(index.convert())
    }

    public actual fun getDouble(index: Int): Double {
        checkIndex(index)
        return actual.doubleAtIndex(index.convert())
    }

    public actual fun getBoolean(index: Int): Boolean {
        checkIndex(index)
        return actual.booleanAtIndex(index.convert())
    }

    public actual fun getBlob(index: Int): Blob? {
        checkIndex(index)
        return actual.blobAtIndex(index.convert())?.asBlob()
    }

    public actual fun getDate(index: Int): Instant? {
        checkIndex(index)
        return actual.dateAtIndex(index.convert())?.toKotlinInstant()
    }

    public actual open fun getArray(index: Int): Array? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.arrayAtIndex(index.convert())?.asArray()
                ?.also { collectionMap[index] = it }
    }

    public actual open fun getDictionary(index: Int): Dictionary? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.dictionaryAtIndex(index.convert())?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    public actual fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    public actual open fun toJSON(): String =
        actual.toJSON()

    actual override operator fun iterator(): Iterator<Any?> =
        ArrayIterator(count)

    private inner class ArrayIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? = getValue(index++)
    }

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Array)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal val Array.actual: CBLArray
    get() = platformState.actual

internal fun CBLArray.asArray() = Array(this)
