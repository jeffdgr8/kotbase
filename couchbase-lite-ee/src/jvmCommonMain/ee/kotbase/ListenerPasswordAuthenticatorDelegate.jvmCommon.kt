@file:JvmName("ListenerPasswordAuthenticatorDelegateJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.ListenerPasswordAuthenticatorDelegate as CBLListenerPasswordAuthenticatorDelegate

internal fun ListenerPasswordAuthenticatorDelegate.convert(): CBLListenerPasswordAuthenticatorDelegate {
    return CBLListenerPasswordAuthenticatorDelegate { username, password ->
        invoke(username, password)
    }
}
