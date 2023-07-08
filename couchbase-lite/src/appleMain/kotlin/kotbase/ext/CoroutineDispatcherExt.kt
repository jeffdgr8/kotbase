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
