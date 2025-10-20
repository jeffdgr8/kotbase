/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import cnames.structs.CBLQuery
import kotbase.internal.DbContext
import kotbase.internal.JsonUtils
import kotbase.internal.fleece.toKString
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.*
import libcblite.*
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

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
            token.remove()
        }
    }

    private fun addNativeChangeListener(holder: QueryChangeListenerHolder) =
        StableRefListenerToken(holder) {
            CBLQuery_AddChangeListener(actual, nativeChangeListener(), it)!!
        }

    private fun nativeChangeListener(): CBLQueryChangeListener {
        return staticCFunction { ref, cblQuery, token ->
            with(ref.to<QueryChangeListenerHolder>()) {
                val change = {
                    try {
                        val resultSet = wrapCBLError { error ->
                            CBLQuery_CopyCurrentResults(cblQuery, token, error)
                        }!!.asResultSet()
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

    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    override fun removeChangeListener(token: ListenerToken) {
        token.remove()
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

    private val memory = object {
        val actual: CPointer<CBLQuery> by lazy {
            database.createQuery(kCBLJSONLanguage, toJSON())
        }
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLQuery_Release(it.actual)
    }

    override val actual: CPointer<CBLQuery>
        get() = memory.actual

    override val dbContext: DbContext by lazy {
        DbContext(database)
    }

    private val database: Database by lazy {
        val from = requireNotNull(from) { "From statement is required." }
        from.source.database
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

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLQuery_Release(it)
    }

    override val dbContext: DbContext = DbContext(database)
}
