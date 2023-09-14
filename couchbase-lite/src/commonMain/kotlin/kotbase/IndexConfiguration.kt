package kotbase

internal expect class IndexConfigurationPlatformState

public expect sealed class IndexConfiguration {

    internal val platformState: IndexConfigurationPlatformState?

    public val expressions: List<String>
}
