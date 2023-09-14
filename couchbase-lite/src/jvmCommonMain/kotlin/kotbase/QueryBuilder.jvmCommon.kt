package kotbase

import com.couchbase.lite.QueryBuilder as CBLQueryBuilder

public actual object QueryBuilder {

    public actual fun select(vararg results: SelectResult): Select =
        Select(CBLQueryBuilder.select(*results.actuals()))

    public actual fun selectDistinct(vararg results: SelectResult): Select =
        Select(CBLQueryBuilder.selectDistinct(*results.actuals()))

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String, database: Database): Query =
        database.createQuery(query)
}
