package kotbase

import cocoapods.CouchbaseLite.CBLIndexConfiguration

internal actual class IndexConfigurationPlatformState(
    internal var actual: CBLIndexConfiguration
)

public actual sealed class IndexConfiguration(actual: CBLIndexConfiguration) {

    internal actual val platformState: IndexConfigurationPlatformState? = IndexConfigurationPlatformState(actual)

    @Suppress("UNCHECKED_CAST")
    public actual val expressions: List<String>
        get() = actual.expressions as List<String>

    override fun equals(other: Any?): Boolean =
        actual.isEqual((other as? IndexConfiguration)?.actual)

    override fun hashCode(): Int =
        actual.hash.toInt()

    override fun toString(): String =
        actual.description ?: super.toString()
}

internal val IndexConfiguration.actual: CBLIndexConfiguration
    get() = platformState!!.actual
