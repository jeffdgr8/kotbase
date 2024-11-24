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

import kotbase.internal.DelegatedClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlin.coroutines.CoroutineContext
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
        @Throws(CouchbaseLiteException::class)
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
        DelegatedListenerToken(actual.addChangeListener(listener.convert()))

    override fun addChangeListener(context: CoroutineContext, listener: QueryChangeSuspendListener): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListener(context[CoroutineDispatcher]?.asExecutor(), listener.convert(scope))
        return SuspendListenerToken(scope, token)
    }

    override fun addChangeListener(scope: CoroutineScope, listener: QueryChangeSuspendListener) {
        val token = actual.addChangeListener(
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
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
