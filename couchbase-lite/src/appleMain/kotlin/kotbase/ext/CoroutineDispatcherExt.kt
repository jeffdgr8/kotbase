package kotbase.ext

import kotlinx.cinterop.convert
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_global_t
import platform.darwin.dispatch_queue_main_t
import platform.darwin.dispatch_queue_t

/**
 * Converts an instance of [CoroutineDispatcher] to an implementation of [dispatch_queue_t].
 *
 * This will either be [dispatch_queue_main_t] for [Dispatchers.Main] or
 * [dispatch_queue_global_t] for other dispatchers.
 */
public fun CoroutineDispatcher.asDispatchQueue(): dispatch_queue_t =
    when (this) {
        Dispatchers.Main -> dispatch_get_main_queue()
        else -> dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.convert(), 0.convert())
    }
