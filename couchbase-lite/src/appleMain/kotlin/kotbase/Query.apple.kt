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

import cocoapods.CouchbaseLite.CBLQuery
import cocoapods.CouchbaseLite.CBLQueryBuilder
import cocoapods.CouchbaseLite.CBLQueryDataSource
import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.CBLQueryJoin
import cocoapods.CouchbaseLite.CBLQueryLimit
import cocoapods.CouchbaseLite.CBLQueryOrdering
import cocoapods.CouchbaseLite.CBLQuerySelectResult
import kotbase.internal.AbstractDelegatedClass
import kotbase.ext.asDispatchQueue
import kotbase.ext.wrapCBLError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

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

    @OptIn(ExperimentalStdlibApi::class)
    override fun addChangeListener(context: CoroutineContext, listener: QueryChangeSuspendListener): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListenerWithQueue(
            context[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, token)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun addChangeListener(scope: CoroutineScope, listener: QueryChangeSuspendListener) {
        val token = actual.addChangeListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            token.remove()
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
    val select: List<CBLQuerySelectResult>,
    val distinct: Boolean = false,
    val from: CBLQueryDataSource? = null,
    val join: List<CBLQueryJoin>? = null,
    val where: CBLQueryExpression? = null,
    val groupBy: List<CBLQueryExpression>? = null,
    val having: CBLQueryExpression? = null,
    val orderBy: List<CBLQueryOrdering>? = null,
    val limit: CBLQueryLimit? = null
) : AbstractQuery() {

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
