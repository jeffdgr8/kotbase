package kotbase

public actual class OrderBy
internal constructor(private val state: QueryState) : Query by state {

    public actual fun limit(limit: Expression): Limit {
        return Limit(state.copy(), limit)
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state.copy(), limit, offset)
    }
}
