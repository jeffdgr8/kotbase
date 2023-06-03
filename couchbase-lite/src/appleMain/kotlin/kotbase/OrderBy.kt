package kotbase

import cocoapods.CouchbaseLite.CBLQueryLimit

public actual class OrderBy
internal constructor(private val state: QueryState) : Query by state {

    public actual fun limit(limit: Expression): Limit {
        state.limit = CBLQueryLimit.limit(limit.actual)
        return Limit(state)
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        state.limit = CBLQueryLimit.limit(limit.actual, offset?.actual)
        return Limit(state)
    }
}
