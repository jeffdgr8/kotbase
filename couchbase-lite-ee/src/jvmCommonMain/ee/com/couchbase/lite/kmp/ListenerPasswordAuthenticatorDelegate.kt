@file:JvmName("ListenerPasswordAuthenticatorDelegateJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun ListenerPasswordAuthenticatorDelegate.convert(): com.couchbase.lite.ListenerPasswordAuthenticatorDelegate {
    return com.couchbase.lite.ListenerPasswordAuthenticatorDelegate { username, password ->
        invoke(username, password)
    }
}
