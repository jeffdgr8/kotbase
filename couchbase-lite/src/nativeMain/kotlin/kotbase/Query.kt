package kotbase

import cnames.structs.CBLQuery
import kotbase.internal.DbContext
import kotbase.internal.JsonUtils
import kotbase.internal.fleece.toKString
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.staticCFunction
import libcblite.*

internal abstract class AbstractQuery : Query {

    abstract val actual: CPointer<CBLQuery>

    abstract val dbContext: DbContext

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
        return ResultSet(resultSet!!, dbContext)
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
        if (token.type == ListenerTokenType.QUERY) {
            if (removeListener(changeListeners, token.index)) {
                CBLListener_Remove(token.actual)
            }
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
        database.createQuery(kCBLJSONLanguage, toJSON())
    }

    override val dbContext: DbContext by lazy {
        DbContext(database)
    }

    private val database: Database by lazy {
        val from = requireNotNull(from) { "From statement is required." }
        from.source
    }

    private fun toJSON(): String {
        val data = buildMap {
            // DISTINCT:
            if (distinct) {
                put("DISTINCT", true)
            }

            // JOIN / FROM:
            put("FROM", buildList {
                add(from?.asJSON())
                join?.forEach {
                    add(it.asJSON())
                }
            })

            // SELECT:
            put("WHAT", select.map { it.asJSON() })

            // WHERE:
            if (where != null) {
                put("WHERE", where?.asJSON())
            }

            // GROUPBY:
            if (groupBy != null) {
                put("GROUP_BY", groupBy?.map { it.asJSON() })
            }

            // HAVING:
            if (having != null) {
                put("HAVING", having?.asJSON())
            }

            // ORDERBY:
            if (orderBy != null) {
                put("ORDER_BY", orderBy?.map { it.asJSON() })
            }

            // LIMIT/OFFSET:
            if (limit != null) {
                put("LIMIT", limit?.limit?.asJSON())
                if (limit?.offset != null) {
                    put("OFFSET", limit?.offset?.asJSON())
                }
            }
        }
        return JsonUtils.toJson(data)
    }
}

internal class DelegatedQuery(
    override val actual: CPointer<CBLQuery>,
    database: Database
) : AbstractQuery() {

    override val dbContext: DbContext = DbContext(database)
}
