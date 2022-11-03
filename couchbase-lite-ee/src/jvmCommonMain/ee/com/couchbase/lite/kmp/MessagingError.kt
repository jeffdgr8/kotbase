package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class MessagingError
internal constructor(actual: com.couchbase.lite.MessagingError) :
    DelegatedClass<com.couchbase.lite.MessagingError>(actual) {

    public actual constructor(error: Exception, recoverable: Boolean) : this(
        com.couchbase.lite.MessagingError(error, recoverable)
    )

    public actual val isRecoverable: Boolean
        get() = actual.isRecoverable

    public actual val error: Exception
        get() = actual.error
}
