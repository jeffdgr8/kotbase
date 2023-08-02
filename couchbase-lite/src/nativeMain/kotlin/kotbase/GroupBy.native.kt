package kotbase

public actual class GroupBy
internal constructor(private val state: QueryState) : Query by state {

    public actual fun having(expression: Expression): Having {
        return Having(state.copy(having = expression))
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
