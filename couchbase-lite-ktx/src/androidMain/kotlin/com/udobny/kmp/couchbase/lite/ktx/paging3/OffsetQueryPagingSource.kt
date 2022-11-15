package com.udobny.kmp.couchbase.lite.ktx.paging3

import androidx.paging.PagingState
import com.couchbase.lite.kmp.Database
import com.couchbase.lite.kmp.Query
import com.molo17.couchbase.lite.kmp.toObjects
import com.udobny.kmp.couchbase.lite.ktx.countResult
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class OffsetQueryPagingSource<RowType : Any>(
    private val queryProvider: (limit: Int, offset: Int) -> Query,
    private val countQuery: Query,
    private val mapper: (Map<String, Any?>) -> RowType?,
    private val database: Database,
    private val context: CoroutineContext,
) : QueryPagingSource<Int, RowType>() {

    override val jumpingSupported get() = true

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, RowType> = withContext(context) {
        val key = params.key ?: 0
        val limit = when (params) {
            is LoadParams.Prepend -> minOf(key, params.loadSize)
            else -> params.loadSize
        }
        val loadResult = database.inBatch {
            val count = countQuery.execute().countResult().toInt()
            val offset = when (params) {
                is LoadParams.Prepend -> maxOf(0, key - params.loadSize)
                is LoadParams.Append -> key
                is LoadParams.Refresh -> if (key >= count) maxOf(0, count - params.loadSize) else key
            }
            val data = queryProvider(limit, offset)
                .also { currentQuery = it }
                .execute()
                .toObjects(mapper)
            val nextPosToLoad = offset + data.size
            LoadResult.Page(
                data = data,
                prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
                nextKey = nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
                itemsBefore = offset,
                itemsAfter = maxOf(0, count - nextPosToLoad),
            )
        }
        if (invalid) LoadResult.Invalid() else loadResult
    }

    override fun getRefreshKey(state: PagingState<Int, RowType>) =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}
