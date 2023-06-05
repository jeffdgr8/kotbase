package kotbase

import cocoapods.CouchbaseLite.CBLQueryLimit

public actual class OrderBy
internal constructor(private val state: QueryState) : Query by state {

    public actual fun limit(limit: Expression): Limit {
        return Limit(state.copy(limit = CBLQueryLimit.limit(limit.actual)))
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state.copy(limit = CBLQueryLimit.limit(limit.actual, offset?.actual)))
    }
}
