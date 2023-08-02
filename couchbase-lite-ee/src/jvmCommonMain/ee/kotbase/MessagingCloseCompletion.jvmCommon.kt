@file:JvmName("MessagingCloseCompletionJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.MessagingCloseCompletion as CBLMessagingCloseCompletion

internal fun CBLMessagingCloseCompletion.convert(): MessagingCloseCompletion {
    return {
        this@convert.complete()
    }
}
