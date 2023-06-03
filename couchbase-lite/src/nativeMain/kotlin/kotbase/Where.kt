package kotbase

public actual class Where
internal constructor(private val state: QueryState) : Query by state {

    public actual fun groupBy(vararg expressions: Expression): GroupBy {
        state.groupBy = expressions.toList()
        return GroupBy(state)
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
