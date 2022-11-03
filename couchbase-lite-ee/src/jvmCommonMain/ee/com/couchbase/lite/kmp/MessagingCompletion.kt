package com.couchbase.lite.kmp

internal fun com.couchbase.lite.MessagingCompletion.convert(): MessagingCompletion {
    return { success, error ->
        complete(success, error?.actual)
    }
}
