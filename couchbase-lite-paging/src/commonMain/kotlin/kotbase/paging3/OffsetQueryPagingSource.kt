/*
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonMain/kotlin/app/cash/sqldelight/paging3/OffsetQueryPagingSource.kt
 */

@file:Suppress("USELESS_IS_CHECK", "IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")

package kotbase.paging3

import app.cash.paging.*
import kotbase.*
import kotbase.ktx.countResult
import kotbase.ktx.selectCount
import kotbase.molo17.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class OffsetQueryPagingSource<RowType : Any>(
    private val select: Select,
    private val database: Database,
    private val queryProvider: From.() -> LimitQueryProvider,
    private val mapper: (Map<String, Any?>) -> RowType,
    private val context: CoroutineContext
) : QueryPagingSource<Int, RowType>() {

    override val jumpingSupported get() = true

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, RowType> = withContext(context) {
        val key = params.key ?: 0
        val limit = when (params) {
            is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
            else -> params.loadSize
        }

        val loadResult = database.inBatch {
            val count = selectCount().from(database)
                .queryProvider()
                .execute()
                .countResult()
                .toInt()
            val offset = when (params) {
                is PagingSourceLoadParamsPrepend<*> -> maxOf(0, key - params.loadSize)
                is PagingSourceLoadParamsAppend<*> -> key
                is PagingSourceLoadParamsRefresh<*> -> if (key >= count) maxOf(0, count - params.loadSize) else key
                else -> error("Unknown PagingSourceLoadParams ${params::class}")
            }
            val data = select.from(database)
                .queryProvider()
                .limit(limit, offset)
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

    override fun getRefreshKey(state: PagingState<Int, RowType>): Int? =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}

internal interface LimitQueryProvider : Query {
    fun limit(limit: Int, offset: Int): Query
}
