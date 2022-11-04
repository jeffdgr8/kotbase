@file:JvmName("MessageEndpointDelegateJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun MessageEndpointDelegate.convert(): com.couchbase.lite.MessageEndpointDelegate {
    return com.couchbase.lite.MessageEndpointDelegate { endpoint ->
        invoke(MessageEndpoint(endpoint, this)).convert()
    }
}
