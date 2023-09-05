package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toKotlinInstant
import kotlinx.datetime.Instant
import com.couchbase.lite.Array as CBLArray

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual open class Array
internal constructor(actual: CBLArray) : DelegatedClass<CBLArray>(actual), Iterable<Any?> {

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count()

    public actual fun getValue(index: Int): Any? =
        actual.getValue(index)?.delegateIfNecessary()

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

    public actual open fun getArray(index: Int): Array? =
        actual.getArray(index)?.asArray()

    public actual open fun getDictionary(index: Int): Dictionary? =
        actual.getDictionary(index)?.asDictionary()

    public actual fun toList(): List<Any?> =
        actual.toList().delegateIfNecessary()

    public actual fun toJSON(): String =
        actual.toJSON()

    actual override operator fun iterator(): Iterator<Any?> = object : Iterator<Any?> {

        private val itr = actual.iterator()

        override fun hasNext(): Boolean = itr.hasNext()

        override fun next(): Any? = itr.next()?.delegateIfNecessary()
    }
}

internal fun CBLArray.asArray() = Array(this)
