package kotbase

public actual class Limit
internal constructor(
    private val state: QueryState,
    internal val limit: Expression,
    internal val offset: Expression? = null
) : Query by state {

    init {
        state.limit = this
    }
}
