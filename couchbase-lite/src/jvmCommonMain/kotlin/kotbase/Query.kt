package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.From as CBLFrom
import com.couchbase.lite.GroupBy as CBLGroupBy
import com.couchbase.lite.Having as CBLHaving
import com.couchbase.lite.Joins as CBLJoins
import com.couchbase.lite.Limit as CBLLimit
import com.couchbase.lite.OrderBy as CBLOrderBy
import com.couchbase.lite.Query as CBLQuery
import com.couchbase.lite.Select as CBLSelect
import com.couchbase.lite.Where as CBLWhere

internal class DelegatedQuery(actual: CBLQuery) : DelegatedClass<CBLQuery>(actual), Query {

    override var parameters: Parameters?
        get() = actual.parameters?.asParameters()
        set(value) {
            actual.parameters = value?.actual
        }

    @Throws(CouchbaseLiteException::class)
    override fun execute(): ResultSet =
        ResultSet(actual.execute())

    @Throws(CouchbaseLiteException::class)
    override fun explain(): String =
        actual.explain()

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert())

    override fun removeChangeListener(token: ListenerToken) =
        actual.removeChangeListener(token)
}

internal fun CBLQuery.asQuery(): Query = when (this) {
    is CBLSelect -> Select(this)
    is CBLFrom -> From(this)
    is CBLJoins -> Joins(this)
    is CBLWhere -> Where(this)
    is CBLGroupBy -> GroupBy(this)
    is CBLHaving -> Having(this)
    is CBLOrderBy -> OrderBy(this)
    is CBLLimit -> Limit(this)
    else -> error("Unknown Query type ${this::class}")
}
