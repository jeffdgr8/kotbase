package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQueryFullTextFunction

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        Expression(CBLQueryFullTextFunction.matchWithIndexName(indexName, text))

    public actual fun rank(indexName: String): Expression =
        Expression(CBLQueryFullTextFunction.rank(indexName))
}
