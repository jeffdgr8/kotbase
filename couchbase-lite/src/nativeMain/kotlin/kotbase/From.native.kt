package kotbase

public actual class From
internal constructor(private val state: QueryState) : Query by state {

    public actual fun join(vararg joins: Join): Joins {
        return Joins(state.copy(join = joins.toList()))
    }

    public actual fun where(expression: Expression): Where {
        return Where(state.copy(where = expression))
    }

    public actual fun groupBy(vararg expressions: Expression): GroupBy {
        return GroupBy(state.copy(groupBy = expressions.toList()))
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