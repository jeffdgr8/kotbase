package kotbase

internal actual class IndexConfigurationPlatformState

public actual sealed class IndexConfiguration(public actual val expressions: List<String>) {

    internal actual val platformState: IndexConfigurationPlatformState? = null
}
