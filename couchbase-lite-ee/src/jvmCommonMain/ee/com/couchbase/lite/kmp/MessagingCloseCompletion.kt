package com.couchbase.lite.kmp

internal fun com.couchbase.lite.MessagingCloseCompletion.convert(): MessagingCloseCompletion {
    return {
        this@convert.complete()
    }
}
