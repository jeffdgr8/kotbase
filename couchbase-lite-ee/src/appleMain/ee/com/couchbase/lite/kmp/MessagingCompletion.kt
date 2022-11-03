package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMessagingError

internal fun ((Boolean, CBLMessagingError?) -> Unit).convert(): MessagingCompletion {
    return { success, error ->
        invoke(success, error?.actual)
    }
}
