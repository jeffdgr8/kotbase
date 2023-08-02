package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.QueryChange as CBLQueryChange

public actual class QueryChange
internal constructor(actual: CBLQueryChange) : DelegatedClass<CBLQueryChange>(actual) {

    public actual val query: Query by lazy {
        actual.query.asQuery()
    }

    public actual val results: ResultSet? by lazy {
        actual.results?.asResultSet()
    }

    public actual val error: Throwable?
        get() = actual.error
}
