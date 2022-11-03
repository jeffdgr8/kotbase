package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMessagingError
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toNSError

public actual class MessagingError
internal constructor(
    actual: CBLMessagingError,
    public actual val error: Exception
) : DelegatedClass<CBLMessagingError>(actual) {

    public actual constructor(error: Exception, recoverable: Boolean) : this(
        CBLMessagingError(error.toNSError(), recoverable),
        error
    )

    public actual val isRecoverable: Boolean
        get() = actual.isRecoverable
}
