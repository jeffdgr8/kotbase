package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLEndpointProtocol
import cocoapods.CouchbaseLite.CBLURLEndpoint

public actual interface Endpoint {

    public val actual: CBLEndpointProtocol
}

internal fun CBLEndpointProtocol.asEndpoint(): Endpoint {
    return when (this) {
        is CBLURLEndpoint -> URLEndpoint(this)
        else -> error("Unknown Endpoint type ${this::class}")
    }
}
