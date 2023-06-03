package kotbase

import com.couchbase.lite.MessagingError
import kotbase.base.DelegatedClass

public actual class MessagingError
internal constructor(actual: com.couchbase.lite.MessagingError) :
    DelegatedClass<MessagingError>(actual) {

    public actual constructor(error: Exception, recoverable: Boolean) : this(
        com.couchbase.lite.MessagingError(error, recoverable)
    )

    public actual val isRecoverable: Boolean
        get() = actual.isRecoverable

    public actual val error: Exception
        get() = actual.error
}
