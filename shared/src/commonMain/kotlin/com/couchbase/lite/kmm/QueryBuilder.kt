package com.couchbase.lite.kmm

public expect object QueryBuilder {

    /**
     * Create a SELECT statement instance that you can use further
     * (e.g. calling the from() function) to construct the complete query statement.
     *
     * @param results The array of the SelectResult object for specifying the returned values.
     * @return A Select object.
     */
    public fun select(vararg results: SelectResult): Select

    /**
     * Create a SELECT DISTINCT statement instance that you can use further
     * (e.g. calling the from() function) to construct the complete query statement.
     *
     * @param results The array of the SelectResult object for specifying the returned values.
     * @return A Select distinct object.
     */
    public fun selectDistinct(vararg results: SelectResult): Select

    /**
     * Create Query from a N1QL string
     *
     * @param query A valid N1QL query.
     * @return database The database against which the query will be run.
     */
    @Throws(CouchbaseLiteException::class)
    public fun createQuery(query: String, database: Database): Query
}
