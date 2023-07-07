/*
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonMain/kotlin/app/cash/sqldelight/paging3/OffsetQueryPagingSource.kt
 */

@file:Suppress("USELESS_IS_CHECK", "IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST", "NOTHING_TO_OVERRIDE", "ABSTRACT_CLASS_MEMBER_NOT_IMPLEMENTED")

package kotbase.paging3

import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadParams
import app.cash.paging.PagingSourceLoadParamsAppend
import app.cash.paging.PagingSourceLoadParamsPrepend
import app.cash.paging.PagingSourceLoadParamsRefresh
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultInvalid
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import kotbase.Database
import kotbase.From
import kotbase.ListenerToken
import kotbase.Query
import kotbase.Select
import kotbase.ktx.countResult
import kotbase.ktx.selectCount
import kotbase.molo17.from
import kotbase.molo17.toObjects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class OffsetQueryPagingSource<RowType : Any>(
    private val select: Select,
    private val database: Database,
    private val queryProvider: From.() -> LimitQueryProvider,
    private val mapper: (Map<String, Any?>) -> RowType,
    private val context: CoroutineContext
) : PagingSource<Int, RowType>() {

    init {
        registerInvalidatedCallback {
            cancelCurrentQueryListener()
        }
    }

    private var currentQuery: Query? = null
    private var listenerToken: ListenerToken? = null

    private fun cancelCurrentQueryListener() {
        listenerToken?.let {
            currentQuery?.removeChangeListener(it)
        }
        listenerToken = null
        currentQuery = null
    }

    override val jumpingSupported get() = true

    override suspend fun load(
        params: PagingSourceLoadParams<Int>
    ): PagingSourceLoadResult<Int, RowType> = withContext(context) {
        cancelCurrentQueryListener()

        val key = params.key ?: 0
        val limit = when (params) {
            is PagingSourceLoadParamsPrepend<*> -> minOf(key, params.loadSize)
            else -> params.loadSize
        }

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
        val query = select.from(database)
            .queryProvider()
            .limit(limit, offset)
            .also { currentQuery = it }
        val results = suspendCancellableCoroutine { continuation ->
            listenerToken = query.addChangeListener(Dispatchers.IO) {
                if (continuation.isActive) { // first results
                    when (val results = it.results) {
                        null -> continuation.resumeWithException(it.error ?: IllegalStateException("No query results or error"))
                        else -> continuation.resume(results)
                    }
                } else { // query changed
                    invalidate()
                }
            }
        }
        when {
            invalid -> PagingSourceLoadResultInvalid<Int, RowType>()
            else -> {
                val data = results.toObjects(mapper)
                val nextPosToLoad = offset + data.size
                PagingSourceLoadResultPage(
                    data = data,
                    prevKey = offset.takeIf { it > 0 && data.isNotEmpty() },
                    nextKey = nextPosToLoad.takeIf { data.isNotEmpty() && data.size >= limit && it < count },
                    itemsBefore = offset,
                    itemsAfter = maxOf(0, count - nextPosToLoad),
                )
            }
        } as PagingSourceLoadResult<Int, RowType>
    }

    override fun getRefreshKey(state: PagingState<Int, RowType>): Int? =
        state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
}

internal interface LimitQueryProvider : Query {
    fun limit(limit: Int, offset: Int): Query
}
