/*
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonTest/kotlin/app/cash/sqldelight/paging3/WithPagingDataDiffer.kt
 */

package kotbase.paging3

import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import app.cash.paging.PagingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object NoopListCallback : ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
    override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
    override fun onInserted(position: Int, count: Int) = Unit
    override fun onRemoved(position: Int, count: Int) = Unit
}

@ExperimentalCoroutinesApi
suspend fun <T : Any> PagingData<T>.withPagingDataDiffer(
    scope: CoroutineScope,
    diffCallback: DiffUtil.ItemCallback<T>,
    block: AsyncPagingDataDiffer<T>.() -> Unit,
) {
    val pagingDataDiffer = AsyncPagingDataDiffer(
        diffCallback,
        NoopListCallback,
        mainDispatcher = Dispatchers.Unconfined,
        workerDispatcher = Dispatchers.Unconfined,
    )
    val job = scope.launch {
        pagingDataDiffer.submitData(this@withPagingDataDiffer)
    }
    // TODO: figure out better way to await database load completion
    delay(100)
    block(pagingDataDiffer)
    job.cancel()
}
