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
package kotbase.ext

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.darwin.DISPATCH_QUEUE_SERIAL
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_attr_t
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t

internal fun CoroutineDispatcher.asDispatchQueue(): dispatch_queue_t =
    when (this) {
        Dispatchers.Main -> dispatch_get_main_queue()
        else -> dispatch_queue_create(
            "${toString()}.asDispatchQueue()",
            DISPATCH_QUEUE_SERIAL as dispatch_queue_attr_t
        )
    }
