@file:JvmName("MessagingCloseCompletionJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun com.couchbase.lite.MessagingCloseCompletion.convert(): MessagingCloseCompletion {
    return {
        this@convert.complete()
    }
}
