package kotbase

import com.couchbase.lite.ReplicatorConnection as CBLReplicatorConnection

internal fun CBLReplicatorConnection.convert(): ReplicatorConnection {
    return object : ReplicatorConnection {

        override fun close(error: MessagingError?) {
            this@convert.close(error?.actual)
        }

        override fun receive(message: Message) {
            this@convert.receive(message.actual)
        }
    }
}
