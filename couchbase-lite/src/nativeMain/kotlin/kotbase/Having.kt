package kotbase

public actual class Having
internal constructor(private val state: QueryState) : Query by state {

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        return OrderBy(state.copy(orderBy = orderings.toList()))
    }

    public actual fun limit(limit: Expression): Limit {
        return Limit(state.copy(), limit)
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state.copy(), limit, offset)
    }
}