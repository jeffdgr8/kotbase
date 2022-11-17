package com.udobny.kmp.couchbase.lite.ktx.paging3

import app.cash.paging.AsyncPagingDataDiffer
import app.cash.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle

private object NoopListCallback : ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
    override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
    override fun onInserted(position: Int, count: Int) = Unit
    override fun onRemoved(position: Int, count: Int) = Unit
}

@ExperimentalCoroutinesApi
fun <T : Any> PagingData<T>.withPagingDataDiffer(
    testScope: TestScope,
    diffCallback: DiffUtil.ItemCallback<T>,
    block: AsyncPagingDataDiffer<T>.() -> Unit,
) {
    val testDispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
    val pagingDataDiffer = AsyncPagingDataDiffer(
        diffCallback,
        NoopListCallback,
        mainDispatcher = testDispatcher,
        workerDispatcher = testDispatcher,
    )
    val job = testScope.launch {
        pagingDataDiffer.submitData(this@withPagingDataDiffer)
    }
    testScope.advanceUntilIdle()
    block(pagingDataDiffer)
    job.cancel()
}
