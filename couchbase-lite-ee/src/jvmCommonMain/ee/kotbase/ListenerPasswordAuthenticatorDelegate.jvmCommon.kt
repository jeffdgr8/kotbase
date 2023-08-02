package kotbase

import com.couchbase.lite.ListenerPasswordAuthenticatorDelegate as CBLListenerPasswordAuthenticatorDelegate

internal fun ListenerPasswordAuthenticatorDelegate.convert(): CBLListenerPasswordAuthenticatorDelegate {
    return CBLListenerPasswordAuthenticatorDelegate { username, password ->
        invoke(username, password)
    }
}
