package com.couchbase.lite.kmp

import java.lang.Exception

internal fun MessageEndpointConnection.convert(): com.couchbase.lite.MessageEndpointConnection =
    NativeMessageEndpointConnection(this)

internal class NativeMessageEndpointConnection(
    internal val original: MessageEndpointConnection
) : com.couchbase.lite.MessageEndpointConnection {

    override fun open(
        connection: com.couchbase.lite.ReplicatorConnection,
        completion: com.couchbase.lite.MessagingCompletion
    ) {
        original.open(connection.convert(), completion.convert())
    }

    override fun close(
        error: Exception?,
        completion: com.couchbase.lite.MessagingCloseCompletion
    ) {
        original.close(error, completion.convert())
    }

    override fun send(
        message: com.couchbase.lite.Message,
        completion: com.couchbase.lite.MessagingCompletion
    ) {
        original.send(Message(message), completion.convert())
    }
}
