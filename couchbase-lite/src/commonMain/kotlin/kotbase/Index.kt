package kotbase

internal expect class IndexPlatformState

/**
 * Index represents an index which could be a value index for regular queries or
 * full-text index for full-text queries (using the match operator).
 */
public expect sealed class Index {

    internal val platformState: IndexPlatformState?
}
