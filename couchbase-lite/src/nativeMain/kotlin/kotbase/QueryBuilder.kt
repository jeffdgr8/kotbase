package kotbase

public actual object QueryBuilder {

    public actual fun select(vararg results: SelectResult): Select =
        Select(QueryState(results.toList()))

    public actual fun selectDistinct(vararg results: SelectResult): Select =
        Select(QueryState(results.toList(), true))

    @Throws(CouchbaseLiteException::class)
    public actual fun createQuery(query: String, database: Database): Query =
        database.createQuery(query)
}
