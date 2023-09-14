package kotbase

import com.couchbase.lite.Index as CBLIndex

internal actual class IndexPlatformState(
    internal val actual: CBLIndex
)

public actual sealed class Index(actual: CBLIndex) {

    internal actual val platformState: IndexPlatformState? = IndexPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual == (other as? Index)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val Index.actual: CBLIndex
    get() = platformState!!.actual
