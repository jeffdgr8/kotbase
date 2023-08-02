package kotbase

import cocoapods.CouchbaseLite.CBLListenerPasswordAuthenticatorBlock

internal fun ListenerPasswordAuthenticatorDelegate.convert(): CBLListenerPasswordAuthenticatorBlock {
    return { username, password ->
        invoke(username!!, password!!.toCharArray())
    }
}
