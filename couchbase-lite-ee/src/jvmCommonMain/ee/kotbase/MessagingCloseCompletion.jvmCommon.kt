package kotbase

import com.couchbase.lite.MessagingCloseCompletion as CBLMessagingCloseCompletion

internal fun CBLMessagingCloseCompletion.convert(): MessagingCloseCompletion {
    return {
        this@convert.complete()
    }
}
