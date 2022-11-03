package com.couchbase.lite.kmp

public actual class MessagingError
actual constructor(
    public actual val error: Exception,
    recoverable: Boolean
) {

    public actual val isRecoverable: Boolean = recoverable
}
