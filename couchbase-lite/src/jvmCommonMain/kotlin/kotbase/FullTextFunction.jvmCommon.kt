package kotbase

import com.couchbase.lite.FullTextFunction as CBLFullTextFunction

public actual object FullTextFunction {

    public actual fun match(indexName: String, text: String): Expression =
        Expression(CBLFullTextFunction.match(indexName, text))

    public actual fun rank(indexName: String): Expression =
        Expression(CBLFullTextFunction.rank(indexName))
}
