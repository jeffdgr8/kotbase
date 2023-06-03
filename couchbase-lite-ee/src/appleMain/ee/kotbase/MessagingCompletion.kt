package kotbase

import cocoapods.CouchbaseLite.CBLMessagingError

internal fun ((Boolean, CBLMessagingError?) -> Unit).convert(): MessagingCompletion {
    return { success, error ->
        invoke(success, error?.actual)
    }
}
