package com.couchbase.lite.kmp

internal fun com.couchbase.lite.ReplicatorConnection.convert(): ReplicatorConnection {
    return object : ReplicatorConnection {

        override fun close(error: MessagingError?) {
            this@convert.close(error?.actual)
        }

        override fun receive(message: Message) {
            this@convert.receive(message.actual)
        }
    }
}
