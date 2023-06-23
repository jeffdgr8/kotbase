package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.MessagingError as CBLMessagingError

public actual class MessagingError
internal constructor(actual: CBLMessagingError) : DelegatedClass<CBLMessagingError>(actual) {

    public actual constructor(error: Exception, recoverable: Boolean) : this(
        CBLMessagingError(error, recoverable)
    )

    public actual val isRecoverable: Boolean
        get() = actual.isRecoverable

    public actual val error: Exception
        get() = actual.error
}
