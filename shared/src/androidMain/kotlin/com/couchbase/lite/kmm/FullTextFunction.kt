package com.couchbase.lite.kmm

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        Expression(com.couchbase.lite.FullTextFunction.match(indexName, text))

    public actual fun rank(indexName: String): Expression =
        Expression(com.couchbase.lite.FullTextFunction.rank(indexName))
}
