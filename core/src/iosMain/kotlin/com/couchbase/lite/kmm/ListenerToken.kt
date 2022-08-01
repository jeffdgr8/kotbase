package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLListenerTokenProtocol

public actual interface ListenerToken {

    public val actual: CBLListenerTokenProtocol
}

internal class DelegatedListenerToken
internal constructor(override val actual: CBLListenerTokenProtocol) : ListenerToken
