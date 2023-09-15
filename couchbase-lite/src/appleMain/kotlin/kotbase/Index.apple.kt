package kotbase

import cocoapods.CouchbaseLite.CBLIndex

internal actual class IndexPlatformState(
    internal val actual: CBLIndex
)

public actual sealed class Index(actual: CBLIndex) {

    internal actual val platformState: IndexPlatformState? = IndexPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? Index)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal val Index.actual: CBLIndex
    get() = platformState!!.actual
