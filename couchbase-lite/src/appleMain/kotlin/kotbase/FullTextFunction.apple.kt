package kotbase

import cocoapods.CouchbaseLite.CBLQueryFullTextFunction

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        DelegatedExpression(CBLQueryFullTextFunction.matchWithIndexName(indexName, text))

    public actual fun rank(indexName: String): Expression =
        DelegatedExpression(CBLQueryFullTextFunction.rank(indexName))
}
