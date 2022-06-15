package com.couchbase.lite.kmm

import com.udobny.kmm.actuals

public actual object QueryBuilder {

    public actual fun select(vararg results: SelectResult): Select =
        Select(com.couchbase.lite.QueryBuilder.select(*results.actuals()))

    public actual fun selectDistinct(vararg results: SelectResult): Select =
        Select(com.couchbase.lite.QueryBuilder.selectDistinct(*results.actuals()))

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String, database: Database): Query =
        database.createQuery(query)
}
