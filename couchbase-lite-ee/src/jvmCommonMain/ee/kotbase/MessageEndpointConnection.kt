package kotbase

import com.couchbase.lite.Message as CBLMessage
import com.couchbase.lite.MessageEndpointConnection as CBLMessageEndpointConnection
import com.couchbase.lite.MessagingCloseCompletion as CBLMessagingCloseCompletion
import com.couchbase.lite.MessagingCompletion as CBLMessagingCompletion
import com.couchbase.lite.ReplicatorConnection as CBLReplicatorConnection

internal fun MessageEndpointConnection.convert(): CBLMessageEndpointConnection =
    NativeMessageEndpointConnection(this)

internal class NativeMessageEndpointConnection(
    internal val original: MessageEndpointConnection
) : CBLMessageEndpointConnection {

    override fun open(
        connection: CBLReplicatorConnection,
        completion: CBLMessagingCompletion
    ) {
        original.open(connection.convert(), completion.convert())
    }

    override fun close(
        error: Exception?,
        completion: CBLMessagingCloseCompletion
    ) {
        original.close(error, completion.convert())
    }

    override fun send(
        message: CBLMessage,
        completion: CBLMessagingCompletion
    ) {
        original.send(Message(message), completion.convert())
    }
}
