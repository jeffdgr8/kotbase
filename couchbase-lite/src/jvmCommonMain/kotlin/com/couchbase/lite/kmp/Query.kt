package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

internal class DelegatedQuery(actual: com.couchbase.lite.Query) :
    DelegatedClass<com.couchbase.lite.Query>(actual), Query {

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

internal fun com.couchbase.lite.Query.asQuery(): Query {
    return when (this) {
        is com.couchbase.lite.Select -> Select(this)
        is com.couchbase.lite.From -> From(this)
        is com.couchbase.lite.Joins -> Joins(this)
        is com.couchbase.lite.Where -> Where(this)
        is com.couchbase.lite.GroupBy -> GroupBy(this)
        is com.couchbase.lite.Having -> Having(this)
        is com.couchbase.lite.OrderBy -> OrderBy(this)
        is com.couchbase.lite.Limit -> Limit(this)
        else -> error("Unknown Query type ${this::class}")
    }
}
