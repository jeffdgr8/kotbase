package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLListenerPasswordAuthenticatorBlock

internal fun ListenerPasswordAuthenticatorDelegate.convert(): CBLListenerPasswordAuthenticatorBlock {
    return { username, password ->
        invoke(username!!, password!!.toCharArray())
    }
}
