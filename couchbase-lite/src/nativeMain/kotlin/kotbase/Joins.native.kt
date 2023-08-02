package kotbase

public actual class Joins
internal constructor(private val state: QueryState) : Query by state {

    public actual fun where(expression: Expression): Where {
        return Where(state.copy(where = expression))
    }

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        return OrderBy(state.copy(orderBy = orderings.toList()))
    }

    public actual fun limit(limit: Expression): Limit {
        return Limit(state.copy(limit = limit))
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state.copy(limit = limit, offset = offset))
    }
}
