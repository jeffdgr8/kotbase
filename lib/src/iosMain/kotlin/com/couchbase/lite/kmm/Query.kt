package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.*
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.internal.testQueue
import com.couchbase.lite.kmm.internal.useTestQueue
import com.udobny.kmm.AbstractDelegatedClass

internal abstract class AbstractQuery : AbstractDelegatedClass<CBLQuery>(), Query {

    abstract override val actual: CBLQuery

    override var parameters: Parameters?
        get() = actual.parameters?.asParameters()
        set(value) {
            actual.parameters = value?.actual
        }

    @Throws(CouchbaseLiteException::class)
    override fun execute(): ResultSet {
        val resultSet = throwError { error ->
            execute(error)
        }
        return ResultSet(resultSet!!)
    }

    @Throws(CouchbaseLiteException::class)
    override fun explain(): String {
        return throwError { error ->
            explain(error)
        }!!
    }

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken {
        return DelegatedListenerToken(
            if (useTestQueue) {
                actual.addChangeListenerWithQueue(testQueue, listener.convert())
            } else {
                actual.addChangeListener(listener.convert())
            }
        )
    }

    override fun removeChangeListener(token: ListenerToken) =
        actual.removeChangeListenerWithToken(token.actual)
}

internal class QueryState(
    var select: List<CBLQuerySelectResult>,
    var distinct: Boolean = false
) : AbstractQuery() {

    var from: CBLQueryDataSource? = null
    var join: List<CBLQueryJoin>? = null
    var where: CBLQueryExpression? = null
    var groupBy: List<CBLQueryExpression>? = null
    var having: CBLQueryExpression? = null
    var orderBy: List<CBLQueryOrdering>? = null
    var limit: CBLQueryLimit? = null

    override val actual: CBLQuery by lazy {
        val from = requireNotNull(from) { "From statement is required." }
        if (distinct) {
            CBLQueryBuilder.selectDistinct(select, from, join, where, groupBy, having, orderBy, limit)
        } else {
            CBLQueryBuilder.select(select, from, join, where, groupBy, having, orderBy, limit)
        }
    }
}

internal class DelegatedQuery(override val actual: CBLQuery) : AbstractQuery()