package kotbase.test

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

suspend fun Mutex.lockWithTimeout(timeout: Duration) =
    withTimeoutOrNull(timeout) { lock() } != null
