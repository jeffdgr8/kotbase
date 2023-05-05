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
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonMain/kotlin/app/cash/sqldelight/paging3/OffsetQueryPagingSource.kt
 * Modified by Jeff Lockhart
 *
 * - Replace SQLDelight database with Couchbase Lite KMP
 */

@file:Suppress(
    "USELESS_IS_CHECK",
    "CAST_NEVER_SUCCEEDS",
    "NOTHING_TO_OVERRIDE",
    "ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED"
)

package com.udobny.kmp.couchbase.lite.ktx.paging3

import app.cash.paging.*
import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.Query
import com.molo17.couchbase.lite.kmp.toObjects
import com.udobny.kmp.couchbase.lite.ktx.countResult
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class OffsetQueryPagingSource<RowType : Any>(
    private val queryProvider: (limit: Int, offset: Int) -> Query,
    private val countQuery: Query,
    private val mapper: (Map<String, Any?>) -> RowType,
    private val database: Database,
    private val context: CoroutineContext
) : QueryPagingSource<Int, RowType>() {

    override val jumpingSupported get() = true

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, RowType> {
        return withContext(context) {
            val key = params.key ?: 0
            val limit = when (params) {
                is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
                else -> params.loadSize
            }
            val loadResult = database.inBatch {
                val count = countQuery.execute().countResult().toInt()
                val offset = when (params) {
                    is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
                    is PagingSourceLoadParamsAppend<*> -> key
                    is PagingSourceLoadParamsRefresh<*> ->
                        if (key >= count) maxOf(0, count - params.loadSize) else key
                    else -> error("Unknown PagingSourceLoadParams ${params::class}")
                }
                val data = queryProvider(limit, offset)
                    .also { currentQuery = it }
                    .execute()
                    .toObjects(mapper)
                val nextPosToLoad = offset + data.size
                PagingSourceLoadResultPage(
                    data = data,
                    prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
                    nextKey = nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
                    itemsBefore = offset,
                    itemsAfter = maxOf(0, count - nextPosToLoad),
                )
            } as PagingSourceLoadResult<Int, RowType>
            if (invalid) {
                PagingSourceLoadResultInvalid<Int, RowType>() as PagingSourceLoadResult<Int, RowType>
            } else {
                loadResult
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RowType>): Int? =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}
