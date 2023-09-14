package kotbase

import com.couchbase.lite.IndexConfiguration as CBLIndexConfiguration

internal actual class IndexConfigurationPlatformState(
    internal val actual: CBLIndexConfiguration
)

public actual sealed class IndexConfiguration(actual: CBLIndexConfiguration) {

    internal actual val platformState: IndexConfigurationPlatformState? = IndexConfigurationPlatformState(actual)

    public actual val expressions: List<String>
        get() = actual.expressions

    override fun equals(other: Any?): Boolean =
        actual == (other as? IndexConfiguration)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val IndexConfiguration.actual: CBLIndexConfiguration
    get() = platformState!!.actual
