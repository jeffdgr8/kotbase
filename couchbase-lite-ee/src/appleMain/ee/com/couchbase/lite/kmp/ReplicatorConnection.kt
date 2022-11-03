package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicatorConnectionProtocol

internal fun CBLReplicatorConnectionProtocol.convert(): ReplicatorConnection {
    return object : ReplicatorConnection {

        override fun close(error: MessagingError?) {
            this@convert.close(error?.actual)
        }

        override fun receive(message: Message) {
            this@convert.receive(message.actual)
        }
    }
}
