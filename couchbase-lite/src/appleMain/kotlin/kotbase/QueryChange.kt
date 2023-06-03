package kotbase

import cocoapods.CouchbaseLite.CBLQueryChange
import kotbase.base.DelegatedClass
import kotbase.ext.toCouchbaseLiteException

public actual class QueryChange
internal constructor(actual: CBLQueryChange) :
    DelegatedClass<CBLQueryChange>(actual) {

    public actual val query: Query by lazy {
        DelegatedQuery(actual.query)
    }

    public actual val results: ResultSet? by lazy {
        actual.results?.asResultSet()
    }

    public actual val error: Throwable? by lazy {
        actual.error?.toCouchbaseLiteException()
    }
}
