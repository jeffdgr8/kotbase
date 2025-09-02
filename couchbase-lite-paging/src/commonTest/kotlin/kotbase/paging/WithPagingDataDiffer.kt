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
 * Based on https://github.com/cashapp/sqldelight/blob/master/extensions/androidx-paging3/src/commonTest/kotlin/app/cash/sqldelight/paging3/WithPagingDataDiffer.kt
 */

package kotbase.paging

import androidx.paging.AsyncPagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.paging.PagingData
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
    delay(300)
    block(pagingDataDiffer)
    job.cancel()
}
