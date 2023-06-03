package kotbase

import cocoapods.CouchbaseLite.CBLMessage
import cocoapods.CouchbaseLite.CBLMessageEndpointConnectionProtocol
import cocoapods.CouchbaseLite.CBLMessagingError
import cocoapods.CouchbaseLite.CBLReplicatorConnectionProtocol
import kotbase.ext.toException
import platform.Foundation.NSError
import platform.darwin.NSObject

internal fun MessageEndpointConnection.convert(): CBLMessageEndpointConnectionProtocol =
    NativeMessageEndpointConnection(this)

internal class NativeMessageEndpointConnection(
    internal val original: MessageEndpointConnection
) : NSObject(), CBLMessageEndpointConnectionProtocol {

    override fun open(
        connection: CBLReplicatorConnectionProtocol,
        completion: (Boolean, CBLMessagingError?) -> Unit
    ) {
        original.open(connection.convert(), completion.convert())
    }

    override fun close(error: NSError?, completion: () -> Unit) {
        original.close(error?.toException(), completion)
    }

    override fun send(message: CBLMessage, completion: (Boolean, CBLMessagingError?) -> Unit) {
        original.send(Message(message), completion.convert())
    }
}
