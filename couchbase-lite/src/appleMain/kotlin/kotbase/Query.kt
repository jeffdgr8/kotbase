package kotbase

import cocoapods.CouchbaseLite.*
import kotbase.base.AbstractDelegatedClass
import kotbase.ext.wrapCBLError

internal abstract class AbstractQuery : AbstractDelegatedClass<CBLQuery>(), Query {

    abstract override val actual: CBLQuery

    override var parameters: Parameters?
        get() = actual.parameters?.asParameters()
        set(value) {
            actual.parameters = value?.actual
        }

    @Throws(CouchbaseLiteException::class)
    override fun execute(): ResultSet {
        val resultSet = wrapCBLError { error ->
            actual.execute(error)
        }
        return ResultSet(resultSet!!)
    }

    @Throws(CouchbaseLiteException::class)
    override fun explain(): String {
        return wrapCBLError { error ->
            actual.explain(error)
        }!!
    }

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addChangeListener(listener.convert())
        )
    }

    override fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        actual.removeChangeListenerWithToken(token.actual)
    }
}

internal data class QueryState(
    var select: List<CBLQuerySelectResult>,
    var distinct: Boolean = false,
    var from: CBLQueryDataSource? = null,
    var join: List<CBLQueryJoin>? = null,
    var where: CBLQueryExpression? = null,
    var groupBy: List<CBLQueryExpression>? = null,
    var having: CBLQueryExpression? = null,
    var orderBy: List<CBLQueryOrdering>? = null,
    var limit: CBLQueryLimit? = null
) : AbstractQuery() {

    override val actual: CBLQuery by lazy {
        val from = requireNotNull(from) { "From statement is required." }
        if (distinct) {
            CBLQueryBuilder
                .selectDistinct(select, from, join, where, groupBy, having, orderBy, limit)
        } else {
            CBLQueryBuilder.select(select, from, join, where, groupBy, having, orderBy, limit)
        }
    }
}

internal class DelegatedQuery(override val actual: CBLQuery) : AbstractQuery()
