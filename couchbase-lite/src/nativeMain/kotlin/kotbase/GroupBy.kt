package kotbase

public actual class GroupBy
internal constructor(private val state: QueryState) : Query by state {

    public actual fun having(expression: Expression): Having {
        state.having = expression
        return Having(state)
    }

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        state.orderBy = orderings.toList()
        return OrderBy(state)
    }

    public actual fun limit(limit: Expression): Limit {
        return Limit(state, limit).also {
            state.limit = it
        }
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state, limit, offset).also {
            state.limit = it
        }
    }
}
