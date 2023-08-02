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
import kotlinx.coroutines.*
import libcblite.*
import kotlin.coroutines.CoroutineContext

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
        val holder = QueryChangeDefaultListenerHolder(listener, this)
        return addNativeChangeListener(holder)
    }

    override fun addChangeListener(context: CoroutineContext, listener: QueryChangeSuspendListener): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = QueryChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        return SuspendListenerToken(scope, token)
    }

    override fun addChangeListener(scope: CoroutineScope, listener: QueryChangeSuspendListener) {
        val holder = QueryChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            removeChangeListener(token)
        }
    }

    private fun addNativeChangeListener(holder: QueryChangeListenerHolder): DelegatedListenerToken {
        val (index, stableRef) = addListener(changeListeners, holder)
        return DelegatedListenerToken(
            CBLQuery_AddChangeListener(
                actual,
                nativeChangeListener(),
                stableRef
            )!!,
            ListenerTokenType.QUERY,
            index
        )
    }

    private fun nativeChangeListener(): CBLQueryChangeListener {
        return staticCFunction { ref, cblQuery, token ->
            with(ref.to<QueryChangeListenerHolder>()) {
                val change = {
                    try {
                        val resultSet = wrapCBLError { error ->
                            CBLQuery_CopyCurrentResults(cblQuery, token, error)!!.asResultSet()
                        }
                        QueryChange(query, resultSet, null)
                    } catch (e: CouchbaseLiteException) {
                        QueryChange(query, null, e)
                    }
                }
                when (this) {
                    is QueryChangeDefaultListenerHolder -> listener(change())
                    is QueryChangeSuspendListenerHolder -> scope.launch {
                        listener(change())
                    }
                }
            }
        }
    }

    override fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            removeChangeListener(token.token)
            token.scope.cancel()
        } else {
            removeChangeListener(token as DelegatedListenerToken)
        }
    }

    private fun removeChangeListener(token: DelegatedListenerToken) {
        if (token.type == ListenerTokenType.QUERY) {
            if (changeListeners.getOrNull(token.index) != null) {
                CBLListener_Remove(token.actual)
                removeListener(changeListeners, token.index)
            }
        } else {
            error("${token.type} change listener can't be removed from Query instance")
        }
    }
}

internal data class QueryState(
    val select: List<SelectResult>,
    val distinct: Boolean = false,
    val from: DataSource? = null,
    val join: List<Join>? = null,
    val where: Expression? = null,
    val groupBy: List<Expression>? = null,
    val having: Expression? = null,
    val orderBy: List<Ordering>? = null,
    val limit: Expression? = null,
    val offset: Expression? = null
) : AbstractQuery() {

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
                put("WHERE", where.asJSON())
            }

            // GROUPBY:
            if (groupBy != null) {
                put("GROUP_BY", groupBy.map { it.asJSON() })
            }

            // HAVING:
            if (having != null) {
                put("HAVING", having.asJSON())
            }

            // ORDERBY:
            if (orderBy != null) {
                put("ORDER_BY", orderBy.map { it.asJSON() })
            }

            // LIMIT/OFFSET:
            if (limit != null) {
                put("LIMIT", limit.asJSON())
                if (offset != null) {
                    put("OFFSET", offset.asJSON())
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
