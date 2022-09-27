package com.couchbase.lite.kmp

import cnames.structs.CBLQuery
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.toExceptionNotNull
import com.couchbase.lite.kmp.internal.wrapCBLError
import kotlinx.cinterop.*
import libcblite.*

internal abstract class AbstractQuery : Query {

    abstract val actual: CPointer<CBLQuery>

    override var parameters: Parameters?
        get() = CBLQuery_Parameters(actual)?.asParameters()
        set(value) {
            CBLQuery_SetParameters(actual, value?.actual)
        }

    @Throws(CouchbaseLiteException::class)
    override fun execute(): ResultSet {
        val resultSet = wrapCBLError { error ->
            CBLQuery_Execute(actual, error)
        }
        return ResultSet(resultSet!!)
    }

    @Throws(CouchbaseLiteException::class)
    override fun explain(): String =
        CBLQuery_Explain(actual).toKString()!!

    private val changeListeners = mutableListOf<QueryChangeListener?>()

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken {
        val index = addChangeListener(changeListeners, listener)
        return DelegatedListenerToken(
            CBLQuery_AddChangeListener(
                actual,
                staticCFunction { idx, _, token ->
                    val lis = changeListeners[idx.toLong().toInt()]!!
                    try {
                        val resultSet = wrapCBLError { error ->
                            CBLQuery_CopyCurrentResults(actual, token, error)!!.asResultSet()
                        }
                        lis(QueryChange(this, resultSet, null))
                    } catch (e: CouchbaseLiteException) {
                        lis(QueryChange(this, null, e))
                    }
                },
                index.toLong().toCPointer<CPointed>()
            )!!,
            ListenerTokenType.QUERY,
            index
        )
    }

    override fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        CBLListener_Remove(token.actual)
        if (token.type == ListenerTokenType.QUERY) {
            removeChangeListener(changeListeners, token.index)
        } else {
            error("${token.type} change listener can't be removed from Query instance")
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
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

    override val actual: CPointer<CBLQuery> by lazy {
        // TODO: get database (from FROM?)
        database.createQuery(this)

        val from = requireNotNull(from) { "From statement is required." }
        if (distinct) {
            CBLQueryBuilder
                .selectDistinct(select, from, join, where, groupBy, having, orderBy, limit)
        } else {
            CBLQueryBuilder.select(select, from, join, where, groupBy, having, orderBy, limit)
        }
    }
}

internal class DelegatedQuery(override val actual: CPointer<CBLQuery>) : AbstractQuery()
