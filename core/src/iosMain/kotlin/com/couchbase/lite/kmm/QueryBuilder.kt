package com.couchbase.lite.kmm

import com.udobny.kmm.actuals

public actual object QueryBuilder {

    public actual fun select(vararg results: SelectResult): Select =
        Select(QueryState(results.actuals()))

    public actual fun selectDistinct(vararg results: SelectResult): Select =
        Select(QueryState(results.actuals(), true))

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String, database: Database): Query =
        database.createQuery(query)
}
