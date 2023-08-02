package kotbase

import cocoapods.CouchbaseLite.CBLArray
import kotbase.base.DelegatedClass
import kotbase.ext.asNumber
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual open class Array
internal constructor(actual: CBLArray) : DelegatedClass<CBLArray>(actual), Iterable<Any?> {

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    public actual fun getValue(index: Int): Any? {
        checkIndex(index)
        return actual.valueAtIndex(index.convert())?.delegateIfNecessary()
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
        return actual.arrayAtIndex(index.convert())?.asArray()
    }

    public actual open fun getDictionary(index: Int): Dictionary? {
        checkIndex(index)
        return actual.dictionaryAtIndex(index.convert())?.asDictionary()
    }

    public actual fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    public actual open fun toJSON(): String =
        actual.toJSON()

    override operator fun iterator(): Iterator<Any?> =
        ArrayIterator(count)

    private inner class ArrayIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? = getValue(index++)
    }

    // Throw IndexOutOfBoundException, avoid Objective-C NSRangeException
    protected fun checkIndex(index: Int) {
        if (index < 0 || index >= count) {
            throw IndexOutOfBoundsException("Array index $index is out of range")
        }
    }
}

internal fun CBLArray.asArray() = Array(this)
