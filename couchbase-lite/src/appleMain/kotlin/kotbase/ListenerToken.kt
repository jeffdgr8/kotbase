package kotbase

import cocoapods.CouchbaseLite.CBLListenerTokenProtocol

public actual interface ListenerToken

internal class DelegatedListenerToken(val actual: CBLListenerTokenProtocol) : ListenerToken
