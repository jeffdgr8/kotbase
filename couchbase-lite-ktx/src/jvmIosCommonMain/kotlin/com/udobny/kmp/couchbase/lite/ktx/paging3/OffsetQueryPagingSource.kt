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
    private val mapper: (Map<String, Any?>) -> RowType?,
    private val database: Database,
    private val context: CoroutineContext,
) : QueryPagingSource<Int, RowType>() {

    override val jumpingSupported get() = true

    override suspend fun load(
        params: PagingSourceLoadParams<Int>,
    ): PagingSourceLoadResult<Int, RowType> = withContext(context) {
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
                is PagingSourceLoadParamsRefresh<*> -> if (key >= count) maxOf(0, count - params.loadSize) else key
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
        }
        (if (invalid) PagingSourceLoadResultInvalid<Int, RowType>() else loadResult) as PagingSourceLoadResult<Int, RowType>
    }

    override fun getRefreshKey(state: PagingState<Int, RowType>) =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}
