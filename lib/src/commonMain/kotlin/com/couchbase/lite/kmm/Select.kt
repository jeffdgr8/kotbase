package com.couchbase.lite.kmm

/**
 * Select represents the SELECT clause of the query for specifying the returning properties in each
 * query result row.
 */
public expect class Select : Query {

    /**
     * Create and chain a FROM component for specifying the data source of the query.
     *
     * @param dataSource the data source.
     * @return the From component.
     */
    public fun from(dataSource: DataSource): From
}
