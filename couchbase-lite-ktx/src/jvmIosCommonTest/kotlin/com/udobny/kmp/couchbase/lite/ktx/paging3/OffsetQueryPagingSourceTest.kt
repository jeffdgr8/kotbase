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
 * Modified by Jeff Lockhart
 *
 * - Replace SQLDelight database with Couchbase Lite KMP
 * - Replace AndroidX Paging with Multiplatform Paging
 */

@file:Suppress("CAST_NEVER_SUCCEEDS", "TYPE_MISMATCH", "UNRESOLVED_REFERENCE")

package com.udobny.kmp.couchbase.lite.ktx.paging3

import androidx.recyclerview.widget.DiffUtil
import app.cash.paging.*
import com.couchbase.lite.kmp.Meta
import com.couchbase.lite.kmp.MutableDocument
import com.couchbase.lite.kmp.Query
import com.molo17.couchbase.lite.kmp.from
import com.molo17.couchbase.lite.kmp.limit
import com.udobny.kmp.couchbase.lite.ktx.BaseTest
import com.udobny.kmp.couchbase.lite.ktx.select
import com.udobny.kmp.couchbase.lite.ktx.selectCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.udobny.kmp.test.IgnoreApple
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.*

// Occasionally hangs on iOS
// https://kotlinlang.slack.com/archives/C1CFAFJSK/p1669167000879859
@IgnoreApple
@ExperimentalCoroutinesApi
class OffsetQueryPagingSourceTest : BaseTest() {

    private val mapper = { json: Map<String, Any?> ->
        TestItem((json["id"] as String).toLong())
    }

    @Test
    fun test_itemCount() = runTest {
        insertItems()

        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        pagingSource.refresh()

        Pager(CONFIG, pagingSourceFactory = { pagingSource })
            .flow
            .first()
            .withPagingDataDiffer(this, testItemDiffCallback) {
                assertEquals(100, itemCount)
            }
    }

    @Test
    fun invalidDbQuery_pagingSourceDoesNotInvalidate() = runTest {
        insertItems()
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        // load once to register db observers
        pagingSource.refresh()
        assertFalse(pagingSource.invalid)

        val result = deleteItem(TestItem(1000))

        // invalid delete. Should have 0 items deleted and paging source remains valid
        assertEquals(0, result)
        assertFalse(pagingSource.invalid)
    }

    @Test
    fun load_initialLoad() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        val result = pagingSource.refresh() as PagingSourceLoadResultPage<Int, TestItem>

        assertContentEquals(ITEMS_LIST.subList(0, 15), result.data)
    }

    @Test
    fun load_initialEmptyLoad() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        val result = pagingSource.refresh() as PagingSourceLoadResultPage<Int, TestItem>

        assertTrue(result.data.isEmpty())

        // now add items
        insertItems()

        // invalidate pagingSource to imitate invalidation from running refreshVersionSync
        pagingSource.invalidate()
        assertTrue(pagingSource.invalid)

        // this refresh should check pagingSource's invalid status, realize it is invalid, and
        // return a PagingSourceLoadResultInvalid
        assertIs<PagingSourceLoadResultInvalid<Int, TestItem>>(pagingSource.refresh())
    }

    @Test
    fun load_initialLoadWithInitialKey() = runTest {
        insertItems()
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        // refresh with initial key = 20
        val result = pagingSource.refresh(key = 20) as PagingSourceLoadResultPage<Int, TestItem>

        // item in pos 21-35 (TestItemId 20-34) loaded
        assertContentEquals(ITEMS_LIST.subList(20, 35), result.data)
    }

    @Test
    fun invalidInitialKey_dbEmpty_returnsEmpty() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        val result = pagingSource.refresh(key = 101) as PagingSourceLoadResultPage<Int, TestItem>

        assertTrue(result.data.isEmpty())
    }

    @Test
    fun invalidInitialKey_keyTooLarge_returnsLastPage() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        val result = pagingSource.refresh(key = 101) as PagingSourceLoadResultPage<Int, TestItem>

        // should load the last page
        assertContentEquals(ITEMS_LIST.subList(85, 100), result.data)
    }

    @Test
    fun invalidInitialKey_negativeKey() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // should throw error when initial key is negative
        val expectedException = assertFailsWith<IllegalArgumentException> {
            pagingSource.refresh(key = -1)
        }
        // default message from Paging 3 for negative initial key
        assertEquals("itemsBefore cannot be negative", expectedException.message)
    }

    @Test
    fun append_middleOfList() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        val result = pagingSource.append(key = 20) as PagingSourceLoadResultPage<Int, TestItem>

        // item in pos 21-25 (TestItemId 20-24) loaded
        assertContentEquals(ITEMS_LIST.subList(20, 25), result.data)
        assertEquals(25, result.nextKey)
        assertEquals(20, result.prevKey)
    }

    @Test
    fun append_availableItemsLessThanLoadSize() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        val result = pagingSource.append(key = 97) as PagingSourceLoadResultPage<Int, TestItem>

        // item in pos 98-100 (TestItemId 97-99) loaded
        assertContentEquals(ITEMS_LIST.subList(97, 100), result.data)
        assertNull(result.nextKey)
        assertEquals(97, result.prevKey)
    }

    @Test
    fun load_consecutiveAppend() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // first append
        val result = pagingSource.append(key = 30) as PagingSourceLoadResultPage<Int, TestItem>

        // TestItemId 30-34 loaded
        assertContentEquals(ITEMS_LIST.subList(30, 35), result.data)
        // second append using nextKey from previous load
        val result2 = pagingSource.append(key = result.nextKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // TestItemId 35 - 39 loaded
        assertContentEquals(ITEMS_LIST.subList(35, 40), result2.data)
    }

    @Test
    fun append_invalidResult() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // first append
        val result = pagingSource.append(key = 30) as PagingSourceLoadResultPage<Int, TestItem>

        // TestItemId 30-34 loaded
        assertContentEquals(ITEMS_LIST.subList(30, 35), result.data)

        // invalidate pagingSource to imitate invalidation from running refreshVersionSync
        pagingSource.invalidate()

        // this append should check pagingSource's invalid status, realize it is invalid, and
        // return a PagingSourceLoadResultInvalid
        val result2 = pagingSource.append(key = result.nextKey)

        assertIs<PagingSourceLoadResultInvalid<Int, TestItem>>(result2)
    }

    @Test
    fun prepend_middleOfList() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        val result = pagingSource.prepend(key = 30) as PagingSourceLoadResultPage<Int, TestItem>

        assertContentEquals(ITEMS_LIST.subList(25, 30), result.data)
        assertEquals(30, result.nextKey)
        assertEquals(25, result.prevKey)
    }

    @Test
    fun prepend_availableItemsLessThanLoadSize() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        val result = pagingSource.prepend(key = 3) as PagingSourceLoadResultPage<Int, TestItem>

        // items in pos 0 - 2 (TestItemId 0 - 2) loaded
        assertContentEquals(ITEMS_LIST.subList(0, 3), result.data)
        assertEquals(3, result.nextKey)
        assertNull(result.prevKey)
    }

    @Test
    fun load_consecutivePrepend() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // first prepend
        val result = pagingSource.prepend(key = 20) as PagingSourceLoadResultPage<Int, TestItem>

        // items pos 16-20 (TestItemId 15-19) loaded
        assertContentEquals(ITEMS_LIST.subList(15, 20), result.data)
        // second prepend using prevKey from previous load
        val result2 = pagingSource.prepend(key = result.prevKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // items pos 11-15 (TestItemId 10 - 14) loaded
        assertContentEquals(ITEMS_LIST.subList(10, 15), result2.data)
    }

    @Test
    fun prepend_invalidResult() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // first prepend
        val result = pagingSource.prepend(key = 20) as PagingSourceLoadResultPage<Int, TestItem>

        // items pos 16-20 (TestItemId 15-19) loaded
        assertContentEquals(ITEMS_LIST.subList(15, 20), result.data)

        // invalidate pagingSource to imitate invalidation from running refreshVersionSync
        pagingSource.invalidate()

        // this prepend should check pagingSource's invalid status, realize it is invalid, and
        // return PagingSourceLoadResultInvalid
        val result2 = pagingSource.prepend(key = result.prevKey)

        assertIs<PagingSourceLoadResultInvalid<Int, TestItem>>(result2)
    }

    @Test
    fun test_itemsBefore() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // for initial load
        val result = pagingSource.refresh(key = 50) as PagingSourceLoadResultPage<Int, TestItem>

        // initial loads items in pos 51 - 65, should have 50 items before
        assertEquals(50, result.itemsBefore)

        // prepend from initial load
        val result2 = pagingSource.prepend(key = result.prevKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // prepend loads items in pos 46 - 50, should have 45 item before
        assertEquals(45, result2.itemsBefore)

        // append from initial load
        val result3 = pagingSource.append(key = result.nextKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // append loads items in position 66 - 70 , should have 65 item before
        assertEquals(65, result3.itemsBefore)
    }

    @Test
    fun test_itemsAfter() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // for initial load
        val result = pagingSource.refresh(key = 30) as PagingSourceLoadResultPage<Int, TestItem>

        // initial loads items in position 31 - 45, should have 55 items after
        assertEquals(55, result.itemsAfter)

        // prepend from initial load
        val result2 = pagingSource.prepend(key = result.prevKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // prepend loads items in position 26 - 30, should have 70 item after
        assertEquals(70, result2.itemsAfter)

        // append from initial load
        val result3 = pagingSource.append(result.nextKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // append loads items in position 46 - 50 , should have 50 item after
        assertEquals(50, result3.itemsAfter)
    }

    @Test
    fun test_getRefreshKey() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        // initial load
        val result = pagingSource.refresh() as PagingSourceLoadResultPage<Int, TestItem>
        // 15 items loaded, assuming anchorPosition = 14 as the last item loaded
        var refreshKey = pagingSource.getRefreshKey(
            PagingState<Int, TestItem>(
                pages = listOf(result),
                anchorPosition = 14,
                config = CONFIG,
                leadingPlaceholderCount = 0,
            ),
        )
        // should load around anchor position
        // Initial load size = 15, refresh key should be (15/2 = 7) items
        // before anchorPosition (14 - 7 = 7)
        assertEquals(7, refreshKey)

        // append after refresh
        val result2 = pagingSource.append(key = result.nextKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        assertContentEquals(ITEMS_LIST.subList(15, 20), result2.data)
        refreshKey = pagingSource.getRefreshKey(
            PagingState<Int, TestItem>(
                pages = listOf(result, result2),
                // 20 items loaded, assume anchorPosition = 19 as the last item loaded
                anchorPosition = 19,
                config = CONFIG,
                leadingPlaceholderCount = 0,
            ),
        )
        // initial load size 15. Refresh key should be (15/2 = 7) items before anchorPosition
        // (19 - 7 = 12)
        assertEquals(12, refreshKey)
    }

    @Test
    fun load_refreshKeyGreaterThanItemCount_lastPage() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        pagingSource.refresh(key = 70)

        deleteItems(40..100)

        // assume user was viewing last item of the refresh load with anchorPosition = 85,
        // initialLoadSize = 15. This mimics how getRefreshKey() calculates refresh key.
        val refreshKey = 85 - (15 / 2)
        assertEquals(78, refreshKey)

        val pagingSource2 = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        val result2 = pagingSource2.refresh(key = refreshKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // database should only have 40 items left. Refresh key is invalid at this point
        // (greater than item count after deletion)
        Pager(CONFIG, pagingSourceFactory = { pagingSource2 })
            .flow
            .first()
            .withPagingDataDiffer(this, testItemDiffCallback) {
                assertEquals(40, itemCount)
            }
        // ensure that paging source can handle invalid refresh key properly
        // should load last page with items 25 - 40
        assertContentEquals(ITEMS_LIST.subList(25, 40), result2.data)

        // should account for updated item count to return correct itemsBefore, itemsAfter,
        // prevKey, nextKey
        assertEquals(25, result2.itemsBefore)
        assertEquals(0, result2.itemsAfter)
        // no append can be triggered
        assertEquals(25, result2.prevKey)
        assertNull(result2.nextKey)
    }

    /**
     * Tests the behavior if user was viewing items in the top of the database and those items
     * were deleted.
     *
     * Currently, if anchorPosition is small enough (within bounds of 0 to loadSize/2), then on
     * invalidation from dropped items at the top, refresh will load with offset = 0. If
     * anchorPosition is larger than loadsize/2, then the refresh load's offset will
     * be 0 to (anchorPosition - loadSize/2).
     *
     * Ideally, in the future Paging will be able to handle this case better.
     */
    @Test
    fun load_refreshKeyGreaterThanItemCount_firstPage() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        pagingSource.refresh()

        Pager(CONFIG, pagingSourceFactory = { pagingSource })
            .flow
            .first()
            .withPagingDataDiffer(this, testItemDiffCallback) {
                assertEquals(100, itemCount)
            }

        // items id 0 - 29 deleted (30 items removed)
        deleteItems(0..29)

        val pagingSource2 = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        // assume user was viewing first few items with anchorPosition = 0 and refresh key
        // clips to 0
        val refreshKey = 0

        val result2 = pagingSource2.refresh(key = refreshKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // database should only have 70 items left
        Pager(CONFIG, pagingSourceFactory = { pagingSource2 })
            .flow
            .first()
            .withPagingDataDiffer(this, testItemDiffCallback) {
                assertEquals(70, itemCount)
            }
        // first 30 items deleted, refresh should load starting from pos 31 (item id 30 - 45)
        assertContentEquals(ITEMS_LIST.subList(30, 45), result2.data)

        // should account for updated item count to return correct itemsBefore, itemsAfter,
        // prevKey, nextKey
        assertEquals(0, result2.itemsBefore)
        assertEquals(55, result2.itemsAfter)
        // no prepend can be triggered
        assertNull(result2.prevKey)
        assertEquals(15, result2.nextKey)
    }

    @Test
    fun load_loadSizeAndRefreshKeyGreaterThanItemCount() = runTest {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        insertItems()
        pagingSource.refresh(key = 30)

        Pager(CONFIG, pagingSourceFactory = { pagingSource })
            .flow
            .first()
            .withPagingDataDiffer(this, testItemDiffCallback) {
                assertEquals(100, itemCount)
            }
        // items id 0 - 94 deleted (95 items removed)
        deleteItems(0..94)

        val pagingSource2 = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        // assume user was viewing first few items with anchorPosition = 0 and refresh key
        // clips to 0
        val refreshKey = 0

        val result2 = pagingSource2.refresh(key = refreshKey)
                as PagingSourceLoadResultPage<Int, TestItem>

        // database should only have 5 items left
        Pager(CONFIG, pagingSourceFactory = { pagingSource2 })
            .flow
            .first()
            .withPagingDataDiffer(this, testItemDiffCallback) {
                assertEquals(5, itemCount)
            }
        // only 5 items should be loaded with offset = 0
        assertContentEquals(ITEMS_LIST.subList(95, 100), result2.data)

        // should recognize that this is a terminal load
        assertEquals(0, result2.itemsBefore)
        assertEquals(0, result2.itemsAfter)
        assertNull(result2.prevKey)
        assertNull(result2.nextKey)
    }

    @Test
    fun test_jumpSupport() {
        val pagingSource = QueryPagingSource(
            countQuery(),
            mapper,
            database,
            EmptyCoroutineContext,
            ::query,
        )
        assertTrue(pagingSource.jumpingSupported)
    }

    private fun query(limit: Int, offset: Int): Query {
        return select(Meta.id)
            .from(database)
            .limit(limit, offset)
    }

    private fun countQuery(): Query =
        selectCount() from database

    private fun insertItems(items: List<TestItem> = ITEMS_LIST) {
        database.inBatch {
            items.forEach {
                database.save(MutableDocument(it.id.toString()))
            }
        }
    }

    private fun deleteItem(item: TestItem): Int {
        return database.getDocument(item.id.toString())?.let { doc ->
            database.delete(doc)
            1
        } ?: 0
    }

    private fun deleteItems(range: IntRange): Int {
        var deleted = 0
        for (id in range) {
            database.getDocument(id.toString())?.let { doc ->
                database.delete(doc)
                deleted++
            }
        }
        return deleted
    }
}

private val CONFIG = PagingConfig(
    pageSize = 5,
    enablePlaceholders = true,
    initialLoadSize = 15,
)

private val ITEMS_LIST = List(100) { TestItem(id = it.toLong()) }

private val testItemDiffCallback = object : DiffUtil.ItemCallback<TestItem>() {

    override fun areItemsTheSame(oldItem: TestItem, newItem: TestItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: TestItem, newItem: TestItem): Boolean =
        oldItem == newItem
}

data class TestItem(val id: Long)

private fun createLoadParam(loadType: LoadType, key: Int?): PagingSourceLoadParams<Int> {
    return when (loadType) {
        LoadType.REFRESH -> PagingSourceLoadParamsRefresh(
            key = key,
            loadSize = CONFIG.initialLoadSize,
            placeholdersEnabled = CONFIG.enablePlaceholders,
        ) as PagingSourceLoadParams<Int>

        LoadType.APPEND -> PagingSourceLoadParamsAppend(
            key = key ?: -1,
            loadSize = CONFIG.pageSize,
            placeholdersEnabled = CONFIG.enablePlaceholders,
        ) as PagingSourceLoadParams<Int>

        LoadType.PREPEND -> PagingSourceLoadParamsPrepend(
            key = key ?: -1,
            loadSize = CONFIG.pageSize,
            placeholdersEnabled = CONFIG.enablePlaceholders,
        ) as PagingSourceLoadParams<Int>

        else -> error("Unknown PagingSourceLoadParams ${loadType::class}")
    }
}

private suspend fun PagingSource<Int, TestItem>.refresh(key: Int? = null): PagingSourceLoadResult<Int, TestItem> =
    load(createLoadParam(LoadType.REFRESH, key))

private suspend fun PagingSource<Int, TestItem>.append(key: Int?): PagingSourceLoadResult<Int, TestItem> =
    load(createLoadParam(LoadType.APPEND, key))

private suspend fun PagingSource<Int, TestItem>.prepend(key: Int?): PagingSourceLoadResult<Int, TestItem> =
    load(createLoadParam(LoadType.PREPEND, key))
