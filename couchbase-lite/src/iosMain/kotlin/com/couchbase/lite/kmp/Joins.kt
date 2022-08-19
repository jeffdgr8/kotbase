package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQueryLimit
import com.udobny.kmp.actuals

public actual class Joins
internal constructor(private val state: QueryState) : Query by state {

    public actual fun where(expression: Expression): Where {
        state.where = expression.actual
        return Where(state)
    }

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        state.orderBy = orderings.actuals()
        return OrderBy(state)
    }

    public actual fun limit(limit: Expression): Limit {
        state.limit = CBLQueryLimit.limit(limit.actual)
        return Limit(state)
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        state.limit = CBLQueryLimit.limit(limit.actual, offset?.actual)
        return Limit(state)
    }
}
