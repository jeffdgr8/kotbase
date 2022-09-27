package com.couchbase.lite.kmp

public actual class Joins
internal constructor(private val state: QueryState) : Query by state {

    public actual fun where(expression: Expression): Where {
        state.where = expression
        return Where(state)
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
