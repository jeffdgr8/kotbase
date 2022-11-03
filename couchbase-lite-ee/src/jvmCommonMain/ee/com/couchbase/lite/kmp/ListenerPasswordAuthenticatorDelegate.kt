package com.couchbase.lite.kmp

internal fun ListenerPasswordAuthenticatorDelegate.convert(): com.couchbase.lite.ListenerPasswordAuthenticatorDelegate {
    return com.couchbase.lite.ListenerPasswordAuthenticatorDelegate { username, password ->
        invoke(username, password)
    }
}
