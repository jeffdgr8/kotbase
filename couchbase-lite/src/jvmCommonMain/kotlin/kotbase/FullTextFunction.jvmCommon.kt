package kotbase

import com.couchbase.lite.FullTextFunction as CBLFullTextFunction

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        DelegatedExpression(CBLFullTextFunction.match(indexName, text))

    public actual fun rank(indexName: String): Expression =
        DelegatedExpression(CBLFullTextFunction.rank(indexName))
}
