/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonMain/kotlin/app/cash/sqldelight/paging3/QueryPagingSource.kt
 * Modified by Jeff Lockhart
 *
 * - Replace SQLDelight database with Couchbase Lite KMP
 */

package com.udobny.kmp.couchbase.lite.ktx.paging3

import app.cash.paging.PagingSource
import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.ListenerToken
import com.couchbase.lite.kmp.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

internal abstract class QueryPagingSource<Key : Any, RowType : Any> :
    PagingSource<Key, RowType>() {

    private var listenerToken: ListenerToken? = null
    private var firstResult = true

    protected var currentQuery: Query? by Delegates.observable(null) { _, old, new ->
        listenerToken?.let {
            old?.removeChangeListener(it)
        }
        firstResult = true
        listenerToken = new?.addChangeListener {
            if (firstResult) {
                firstResult = false
            } else {
                invalidate()
            }
        }
    }

    init {
        registerInvalidatedCallback {
            listenerToken?.let {
                currentQuery?.removeChangeListener(it)
            }
            listenerToken = null
            currentQuery = null
        }
    }
}

/**
 * Create a [PagingSource] that pages through results according to queries generated by
 * [queryProvider]. Queries returned by [queryProvider] should expect to do SQL offset/limit
 * based paging. For that reason, [countQuery] is required to calculate pages and page offsets.
 *
 * An example query returned by [queryProvider] could look like:
 *
 * ```sql
 * SELECT value FROM numbers
 * LIMIT 10
 * OFFSET 100;
 * ```
 *
 * Queries will be executed on [context].
 */
@Suppress("FunctionName")
public fun <RowType : Any> QueryPagingSource(
    countQuery: Query,
    mapper: (Map<String, Any?>) -> RowType,
    database: Database,
    context: CoroutineContext = Dispatchers.IO,
    queryProvider: (limit: Int, offset: Int) -> Query,
): PagingSource<Int, RowType> = OffsetQueryPagingSource(
    queryProvider,
    countQuery,
    mapper,
    database,
    context
)