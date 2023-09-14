package kotbase

import cocoapods.CouchbaseLite.CBLQueryFullTextFunction

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        ExpressionImpl(CBLQueryFullTextFunction.matchWithIndexName(indexName, text))

    public actual fun rank(indexName: String): Expression =
        ExpressionImpl(CBLQueryFullTextFunction.rank(indexName))
}
