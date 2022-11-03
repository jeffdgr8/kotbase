package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMessageEndpoint
import cocoapods.CouchbaseLite.CBLMessageEndpointConnectionProtocol
import cocoapods.CouchbaseLite.CBLMessageEndpointDelegateProtocol

internal fun MessageEndpointDelegate.convert(
    delegate: MessageEndpointDelegate
): CBLMessageEndpointDelegateProtocol {
    return object : CBLMessageEndpointDelegateProtocol {

        override fun createConnectionForEndpoint(
            endpoint: CBLMessageEndpoint
        ): CBLMessageEndpointConnectionProtocol =
            invoke(MessageEndpoint(endpoint, delegate)).convert()
    }
}
