package com.couchbase.lite.kmp

import cnames.structs.CBLQuery
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.wrapCBLError
import com.udobny.kmp.to
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

    private val changeListeners = mutableListOf<StableRef<QueryChangeListenerHolder>?>()

    override fun addChangeListener(listener: QueryChangeListener): ListenerToken {
        val holder = QueryChangeListenerHolder(listener, this)
        val (index, stableRef) = addListener(changeListeners, holder)
        return DelegatedListenerToken(
            CBLQuery_AddChangeListener(
                actual,
                staticCFunction { ref, query, token ->
                    with(ref.to<QueryChangeListenerHolder>()) {
                        try {
                            val resultSet = wrapCBLError { error ->
                                CBLQuery_CopyCurrentResults(query, token, error)!!.asResultSet()
                            }
                            this.listener(QueryChange(this.query, resultSet, null))
                        } catch (e: CouchbaseLiteException) {
                            this.listener(QueryChange(this.query, null, e))
                        }
                    }
                },
                stableRef
            )!!,
            ListenerTokenType.QUERY,
            index
        )
    }

    override fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        CBLListener_Remove(token.actual)
        if (token.type == ListenerTokenType.QUERY) {
            removeListener(changeListeners, token.index)
        } else {
            error("${token.type} change listener can't be removed from Query instance")
        }
    }
}

@Suppress("MemberVisibilityCanBePrivate")
internal class QueryState(
    var select: List<SelectResult>,
    var distinct: Boolean = false
) : AbstractQuery() {

    var from: DataSource? = null
    var join: List<Join>? = null
    var where: Expression? = null
    var groupBy: List<Expression>? = null
    var having: Expression? = null
    var orderBy: List<Ordering>? = null
    var limit: Limit? = null

    override val actual: CPointer<CBLQuery> by lazy {
        val from = requireNotNull(from) { "From statement is required." }
        from.source.createQuery(kCBLJSONLanguage, toJSON())
    }

    private fun toJSON(): String {
        return MutableDictionary().apply {
            // DISTINCT:
            if (distinct) {
                setBoolean("DISTINCT", true)
            }

            // JOIN / FROM:
            setArray("FROM", MutableArray().apply {
                addDictionary(from?.asJSON())
                join?.forEach {
                    addDictionary(it.asJSON())
                }
            })

            // SELECT:
            setArray("WHAT", MutableArray().apply {
                select.forEach {
                    addValue(it.asJSON())
                }
            })

            // WHERE:
            if (where != null) {
                setValue("WHERE", where)
            }

            // GROUPBY:
            if (groupBy != null) {
                setArray("GROUP_BY", MutableArray().apply {
                    groupBy?.forEach {
                        addValue(it.asJSON())
                    }
                })
            }

            // HAVING:
            if (having != null) {
                setValue("HAVING", having?.asJSON())
            }

            // ORDERBY:
            if (orderBy != null) {
                setArray("ORDER_BY", MutableArray().apply {
                    orderBy?.forEach {
                        addValue(it.asJSON())
                    }
                })
            }

            // LIMIT/OFFSET:
            if (limit != null) {
                setValue("LIMIT", limit?.limit?.asJSON())
                if (limit?.offset != null) {
                    setValue("OFFSET", limit?.offset?.asJSON())
                }
            }
        }.toJSON()
    }
}

internal class DelegatedQuery(override val actual: CPointer<CBLQuery>) : AbstractQuery()