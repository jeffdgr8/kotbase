@file:JvmName("MessagingCompletionJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.MessagingCompletion as CBLMessagingCompletion

internal fun CBLMessagingCompletion.convert(): MessagingCompletion {
    return { success, error ->
        complete(success, error?.actual)
    }
}
