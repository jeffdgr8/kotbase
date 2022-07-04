package com.couchbase.lite.kmm

public actual class Select
internal constructor(private val state: QueryState) : Query by state {

    public actual fun from(dataSource: DataSource): From {
        state.from = dataSource.actual
        return From(state)
    }
}
