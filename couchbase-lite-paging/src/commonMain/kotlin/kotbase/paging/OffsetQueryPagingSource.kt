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

/*
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonMain/kotlin/app/cash/sqldelight/paging3/OffsetQueryPagingSource.kt
 */

package kotbase.paging

import androidx.paging.*
import kotbase.*
import kotbase.Collection
import kotbase.ktx.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class OffsetQueryPagingSource<RowType : Any>(
    private val select: Select,
    private val collection: Collection,
    private val queryProvider: From.() -> LimitRouter,
    private val context: CoroutineContext,
    private val initialOffset: Int,
    private val mapMapper: ((Map<String, Any?>) -> RowType)? = null,
    private val jsonStringMapper: ((String) -> RowType)? = null,
) : PagingSource<Int, RowType>() {

    init {
        require(mapMapper != null || jsonStringMapper != null) { "At least one mapper must be not null" }

        registerInvalidatedCallback {
            cancelCurrentQueryListener()
        }
    }

    private var currentQuery: Query? = null
    private var listenerToken: ListenerToken? = null

    private fun cancelCurrentQueryListener() {
        listenerToken?.remove()
        listenerToken = null
        currentQuery = null
    }

    override val jumpingSupported get() = true

    private fun ResultSet.toObjects(): List<RowType> {
        return if (mapMapper != null) {
            toObjects(mapMapper)
        } else {
            toObjects(jsonStringMapper!!)
        }
    }

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, RowType> = withContext(context) {
        cancelCurrentQueryListener()

        val key = params.key ?: initialOffset
        val limit = when (params) {
            is LoadParams.Prepend<*> -> minOf(key, params.loadSize)
            else -> params.loadSize
        }

        val count = selectCount().from(collection)
            .queryProvider()
            .execute()
            .countResult()
            .toInt()
        val offset = when (params) {
            is LoadParams.Prepend -> maxOf(0, key - params.loadSize)
            is LoadParams.Append -> key
            is LoadParams.Refresh -> if (key >= count - params.loadSize) maxOf(0, count - params.loadSize) else key
        }
        val results = select.from(collection)
            .queryProvider()
            .limit(limit, offset)
            .getAndListenForResults()
        val data = results.toObjects()
        val nextPosToLoad = offset + data.size
        when {
            invalid -> LoadResult.Invalid()
            else -> LoadResult.Page(
                data = data,
                prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
                nextKey = nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
                itemsBefore = offset,
                itemsAfter = maxOf(0, count - nextPosToLoad),
            )
        }
    }

    private suspend fun Query.getAndListenForResults(): ResultSet {
        currentQuery = this
        return suspendCancellableCoroutine { continuation ->
            listenerToken = addChangeListener(context) {
                if (continuation.isActive) {
                    when (val results = it.results) {
                        null -> continuation.resumeWithException(it.error ?: IllegalStateException("No query results or error"))
                        else -> continuation.resume(results)
                    }
                } else {
                    invalidate()
                }
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RowType>): Int? =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}
