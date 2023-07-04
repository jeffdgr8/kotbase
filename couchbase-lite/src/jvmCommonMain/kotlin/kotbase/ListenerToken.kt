package kotbase

import kotlinx.coroutines.CoroutineScope

public actual typealias ListenerToken = com.couchbase.lite.ListenerToken

internal class SuspendListenerToken(
    val scope: CoroutineScope,
    val actual: ListenerToken
) : ListenerToken
