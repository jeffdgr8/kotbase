package kotbase

import cocoapods.CouchbaseLite.CBLListenerTokenProtocol
import kotlinx.coroutines.CoroutineScope

public actual interface ListenerToken

internal class DelegatedListenerToken(val actual: CBLListenerTokenProtocol) : ListenerToken

internal class SuspendListenerToken(
    val scope: CoroutineScope,
    val token: DelegatedListenerToken
) : ListenerToken
